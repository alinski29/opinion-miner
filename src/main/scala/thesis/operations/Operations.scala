package thesis.operations

import scala.collection.mutable.ListBuffer
import scala.util.Try
import scala.math.log10

object UtilityFunctions {
  import org.clulab.processors.fastnlp.FastNLPProcessor
  def cleanString(string: String) = string.replace("\"", "")
  lazy val proc = new FastNLPProcessor
  def lemmatize(word: String): String = {
    val doc = proc.mkDocument(word)
    proc.tagPartsOfSpeech(doc)
    proc.lemmatize(doc)
    val lemma = doc.sentences.map(_.lemmas.get).map(x => x.mkString(" ")).mkString("")
    doc.clear()
    lemma
  }
}

object VectorDistances {
  import thesis.operations.UtilityFunctions._
  private lazy val aspectVectors = io.Source.fromFile("src/main/resources/final/aspectVectors.csv").getLines().map(_.split(","))
    .map(x => cleanString(x(0)) -> x.tail.toList.map(_.toDouble)).toMap
  private def dotProduct(x: List[Double], y: List[Double]) = (for ((a, b) <- x zip y) yield a * b) sum
  private def magnitude(x: List[Double]) = math.sqrt(x map (i => i * i) sum)
  private def cosineSimilarity(x: List[Double], y: List[Double]): Double = {
    require(x.size == y.size)
    dotProduct(x, y) / (magnitude(x) * magnitude(y))
  }

  def computeWordDistance(word1: String, word2: String, vectorMap: Map[String, List[Double]]): Double = {
    val wordVectors = (vectorMap.getOrElse(word1, vectorMap.getOrElse(lemmatize(word1), List())),
      vectorMap.getOrElse(word2, vectorMap.getOrElse(lemmatize(word2), List())))
    if (wordVectors._1.nonEmpty && wordVectors._2.nonEmpty) {
      cosineSimilarity(wordVectors._1, wordVectors._2)
    } else 0.0
  }

  def computeTopicDistance(word: String, topics: collection.mutable.Map[String, List[String]]) = {
    val distances = topics.map(topic => (topic._1, topic._2.map(w => computeWordDistance(word, w, aspectVectors))))
    distances.map(x => (x._1, x._2.filterNot(w => w.equals(0.0)))).toList
      .map(x => (x._1, x._2.sorted(Ordering.Double.reverse).take(5)))
      .map(x => (x._1, x._2.sum / x._2.length)).toMap
  }
}


  //Load resources here
object AspectMatcher {
    import thesis.operations.UtilityFunctions._
    import thesis.structure.Wrappers.Aspect
    private val frequentVectors = io.Source.fromFile("src/main/resources/final/aspectTopicMap.csv").getLines().drop(1).map(_.split(","))
      .map(x => cleanString(x(0)) -> Aspect(word = cleanString(x(0)), topic = cleanString(x(1)), topic_secondary = cleanString(x(3)),
        wordVector = x.tail.drop(3).toList.map(_.toDouble))).toMap
    private val aspectTopics = frequentVectors.map(_._2).map(x => x.topic -> x.word)
      .groupBy(_._1).map(x => x._1 -> x._2.map(_._2).toList)
    private val foodTopics = frequentVectors.filter(_._2.topic == "Food")
      .map(_._2).map(x => x.topic_secondary -> x.word)
      .groupBy(_._1).map(x => x._1 -> x._2.map(_._2).toList)

    def classifyAspect(aspect: String): Option[(String, String)] = {
      val lookup = frequentVectors.get(aspect)
      lookup match {
        case Some(aspect) => Some((aspect.topic, aspect.topic_secondary))
        case None => None
      }
    }
  }


object SentimentOperations {
  import thesis.structure.Wrappers.Sentiment

  object SentAssigner {

    import VectorDistances._

    private val sentimentVectors = io.Source.fromFile("src/main/resources/final/sentiment_vectors_ratings.csv").getLines().drop(1)
      .map(_.split(",").map(_.trim)).map(x => x.head ->
      Sentiment(word = x(1), frequency = x(2).toInt, avg_rating = x(3).toDouble,
        composite_rating = x(4).toDouble, avg_rating_pos = x(5).toDouble,
        avg_rating_neg = x(6).toDouble, wordVector = x.tail.drop(6).toList.map(_.toDouble))).toMap
    private val frequentSentimentVectors: Map[String, Sentiment] = sentimentVectors.filter(_._2.frequency > 200)

    def getSentimentScore(sentiment: String, polarity: String): Option[(Double, Double)] = {
      //if word in sentimentVectors and freq > 200 ... return score
      val lookup = frequentSentimentVectors.get(sentiment)
      lookup match {
        case Some(sentClass) => polarity match {
          case "positive" => {
            if (sentClass.composite_rating > sentClass.avg_rating_pos) {
              Some(sentClass.composite_rating, sentClass.composite_rating)
            } else Some((sentClass.composite_rating, sentClass.avg_rating_pos))
          }
          case "very positive" => {
            if (sentClass.composite_rating > sentClass.avg_rating_pos) {
              Some(sentClass.composite_rating, sentClass.composite_rating)
            } else Some((sentClass.composite_rating, sentClass.avg_rating_pos))
          }
          case "negative" => {
            if (sentClass.composite_rating < sentClass.avg_rating_neg) {
              Some(sentClass.composite_rating, sentClass.composite_rating)
            } else Some((sentClass.composite_rating, sentClass.avg_rating_neg))
          }
          case "very negative" => {
            if (sentClass.composite_rating < sentClass.avg_rating_neg) {
              Some(sentClass.composite_rating, sentClass.composite_rating)
            } else Some((sentClass.composite_rating, sentClass.avg_rating_neg))
          }
          case _ => Some(sentClass.composite_rating, sentClass.composite_rating)
        }
        case None => assignRatingToInfrequentSentiment(sentiment)
      }
    }

    private def assignRatingToInfrequentSentiment(sentiment: String): Option[(Double, Double)] = {
      val distanceAccu = ListBuffer[(String, String, Double, Double)]()
      val sentimentVector = sentimentVectors.get(sentiment)
      sentimentVector match {
        case Some(wordVector) => {
          for (value <- frequentSentimentVectors) {
            val distance = (sentiment, value._1, value._2.composite_rating,
              Try(computeWordDistance(sentiment, value._1, frequentSentimentVectors.map(x => x._1 -> x._2.wordVector))).toOption.getOrElse(0.0))
            if (distance._4 > 0.70) distanceAccu += distance
          }
          if (distanceAccu.nonEmpty) {
            val estimatedSentiment = distanceAccu.map(x => x._3).sum / distanceAccu.length
            Some((estimatedSentiment, estimatedSentiment))
          } else None
        }
        case None => None
      }
    }
  }

  object SentFunctions {

    def polarityToInt(polarity: String): Double = {
      polarity match {
        case "very negative" => 1
        case "negative" => 2
        case "neutral" => 3
        case "positive" => 4
        case "very positive" => 5
        case _ => 0
      }
    }

    def ratingToPolarity(rating: Int): (Int, Int) = {
      rating match {
        case 1 => (0, 2)
        case 2 => (0, 1)
        case 4 => (1, 0)
        case 5 => (2, 0)
        case _ => (1, 1)
      }
    }

    def sentimentToIncrement(polarity: String): (Int, Int) = {
      polarity match {
        case "very negative" => (0, 2)
        case "negative" => (0, 1)
        case "positive" => (1, 0)
        case "very positive" => (2, 0)
        case _ => (1, 1)
      }
    }

    def computePMI(wordPolarityFrequency: Double, polarityFrequency: Double, wordFrequency: Double, size: Double): Double = {
      var log2 = (x: Double) => log10(x)/log10(2.0)
      log2((wordPolarityFrequency * size)/(wordFrequency * polarityFrequency))
    }
  }

}