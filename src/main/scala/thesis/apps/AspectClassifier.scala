package thesis.apps

import java.io.FileWriter
import com.github.tototoshi.csv.CSVWriter
import thesis.operations.SentimentOperations.SentAssigner.getSentimentScore
import thesis.operations.AspectMatcher.classifyAspect
import thesis.structure.Wrappers.ExtendedPhrase

/*
Takes the CSV file with Phrases and classifies the aspect into an aspect topic and
 a secondary topic, if the primary topic is food. It also assings 2 sentiment scores for each sentiment word.
 Finally, it writes a csv file
 @Fixme: The aspect classified should get data directly from database and put it into a new collection
 */

object AspectClassifier extends App {

  val inputFile = scala.io.Source.fromFile("src/main/resources/staging/phrases_full.csv").getLines()
  val fileLength = scala.io.Source.fromFile("src/main/resources/staging/phrases_full.csv").getLines().length
  val fw = new FileWriter("src/main/resources/final/phrases_classified.csv", true)
  val writer = CSVWriter.open(fw)
  var counter = 0

  for (line <- inputFile) {
    val phrase = line.split(",").map(_.trim)
    val aspect = phrase(7)
    val sentiment = phrase(9)
    val polarity = phrase(12).toLowerCase()
    val aspect_topic = classifyAspect(aspect)
    val sentiment_score = getSentimentScore(sentiment, polarity)

    aspect_topic match {
      case Some(topic) =>
        sentiment_score match {
          case Some(score) => println(aspect, topic._1 + ":" + topic._2, sentiment, score)
            val newPhrase = ExtendedPhrase(
              phrase_id = phrase(0),
              rev_id = phrase(1),
              user_id = phrase(2),
              business_id = phrase(3),
              date = phrase(4),
              lang = phrase(5),
              sentence_no = phrase(6).toInt,
              aspect = aspect.toLowerCase,
              aspect_pos = phrase(8),
              aspect_topic = topic._1.toLowerCase,
              aspect_topic_secondary = topic._2.toLowerCase,
              sentiment = sentiment.toLowerCase,
              sentiment_pos = phrase(10),
              sentiment_score = score._1,
              sentiment_score2 = score._2,
              dependency_type = phrase(11),
              polarity = polarity.toLowerCase,
              rating = phrase(13).toInt)
              writer.writeRow(newPhrase.toList)
          case None => println(s"No sentiment score found for $sentiment")
        }
      case None => println(s"No aspect topic found for: $aspect ($sentiment)")
    }
    counter += 1
    println(s"Progress: $counter / $fileLength")
  }
  fw.close()
}

