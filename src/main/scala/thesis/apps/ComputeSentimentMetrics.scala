package thesis.apps

import java.io.FileWriter

import com.github.tototoshi.csv.CSVWriter
import thesis.operations.SentimentOperations.SentFunctions._
import thesis.operations.UtilityFunctions._

/*
Takes the phrases.csv export (file with all phrases) and calculates different metrics such as: frequency,
 avg score, avg polarity and exports 2 files in /staging folder
*/

object ComputeSentimentMetrics {

  val phrasesFile = io.Source.fromFile("src/main/resources/staging/phrases.csv").getLines()
  val aspectTopicMap = io.Source.fromFile("src/main/resources/final/aspectTopicMap.csv").getLines().map(_.split(","))
    .map(x => cleanString(x(0)) -> cleanString(x(1))).toMap

  def main(args: Array[String]): Unit = {

    val sentimentRating = collection.mutable.Map[String, (Double, Double, Double, Double, Int, Int, Int, Int)]()
    val sentimentSentencePolarity = collection.mutable.Map[String, (Double, Int, Int, Int)]()

    for(line <- phrasesFile) {
      val parts = line.split(",").toList.map(_.trim)
      val aspect = parts(7)
      val sentiment = parts(9)
      val polarity = parts(12).toLowerCase
      val rating = parts(13).toInt
      val polarityScore = polarityToInt(polarity)
      val polarityRating = ratingToPolarity(rating)
      val aspectExists = aspectTopicMap.contains(aspect)

      if (aspectExists && polarity.nonEmpty) {
        if (sentimentRating.contains(sentiment)) {
          val oldRating = sentimentRating.getOrElse(sentiment, (0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0))
          val newRating =  polarity match {
            case "positive" | "very positive" => {
              ((oldRating._1 * oldRating._5 + rating.toDouble) / (oldRating._5 + 1).toLong, //Avg rating (overall)
                (oldRating._2 * oldRating._6 + rating.toDouble) / (oldRating._6 + 1).toLong, //Avg rating (positive)
                oldRating._3, //Avg rating (negative)
                oldRating._4, //Avg rating (neutral)
                oldRating._5 + 1, //Frequency (overall)
                oldRating._6 + polarityRating._1, //Frequency (positive)
                oldRating._7 + polarityRating._2, // Frequency (negative)
                oldRating._8) //Frequency (neutral)
            }
            case "negative" | "very negative" => {
              ((oldRating._1 * oldRating._5 + rating.toDouble) / (oldRating._5 + 1).toLong, //Avg rating (overall)
                oldRating._2, //Avg rating (positive)
                (oldRating._3 * oldRating._7 + rating.toDouble) / (oldRating._7 + 1).toLong, //Avg rating (negative)
                oldRating._4, //Avg rating (neutral)
                oldRating._5 + 1, //Frequency (overall)
                oldRating._6 + polarityRating._1, //Frequency (positive)
                oldRating._7 + polarityRating._2, // Frequency (negative)
                oldRating._8) //Frequency (neutral)
            }
            case "neutral" => {
              ((oldRating._1 * oldRating._5 + rating.toDouble) / (oldRating._5 + 1).toLong, //Avg rating (overall)
                oldRating._2, //Avg rating (positive)
                oldRating._3, //Avg rating (negative
                (oldRating._4 * oldRating._8 + rating.toDouble) / (oldRating._8 + 1).toLong, //Avg rating (neutral)
                oldRating._5 + 1, //Frequency (overall)
                oldRating._6 + polarityRating._1, //Frequency (positive)
                oldRating._7 + polarityRating._2, // Frequency (negative)
                oldRating._8 + 1) // Frequency (neutral)
            }
          }
          sentimentRating.update(sentiment, newRating)
        } else {
          val newRating = polarity match {
            case "positive" | "very positive" => (rating.toDouble, rating.toDouble, 0.0, 0.0, 1, polarityRating._1, polarityRating._2, 0)
            case "negative" | "very negative" => (rating.toDouble, 0.0, rating.toDouble, 0.0, 1, polarityRating._1, polarityRating._2, 0)
            case "neutral" => (rating.toDouble, 0.0, 0.0, rating.toDouble, 1, polarityRating._1, polarityRating._2, 1)
          }
          sentimentRating += sentiment -> newRating
          }

          val increments = sentimentToIncrement(polarity)
          if (sentimentSentencePolarity.contains(sentiment)) {
            val oldRating = sentimentSentencePolarity.getOrElse(sentiment, (0.0, 0, 0, 0))
            val newRating = ((oldRating._1 * oldRating._2 + polarityScore) / (oldRating._2 + 1), oldRating._2 + 1,
              oldRating._3 + increments._1, oldRating._4 + increments._2)
            sentimentSentencePolarity.update(sentiment, newRating)
          } else {
            sentimentSentencePolarity += sentiment -> (polarityScore.toDouble, 1, increments._1, increments._2)
          }
        }
      }

    val ratingList = sentimentRating.map(x => (x._1, x._2._1, x._2._2, x._2._3, x._2._4, x._2._5, x._2._6, x._2._7)).toList.sortBy(_._6).reverse
    val polarityList = sentimentSentencePolarity.map(x => (x._1, x._2._1, x._2._2, x._2._3, x._2._4)).toList.sortBy(_._3).reverse

    val positiveFrequencyR = ratingList.map(_._7).sum
    val negativeFrequencyR = ratingList.map(_._8).sum
    val sizeR = positiveFrequencyR + negativeFrequencyR

    val positiveFrequencyL = polarityList.map(_._4).sum
    val negativeFrequencyL = polarityList.map(_._5).sum
    val sizeL = positiveFrequencyL + negativeFrequencyL

    //List[word, avg rating, size, PMI]
    val PMIRatings = ratingList.map(x =>
      (x._1, x._2, x._3, x._4, x._5, x._6,
      computePMI(x._7, positiveFrequencyR, x._7 + x._8, sizeR) -
        computePMI(x._8, negativeFrequencyR, x._7 + x._8, sizeR))).
      filter(_._6 > 30).sortBy(_._6).reverse.map(
      x => List(x._1, lemmatize(x._1), x._2, x._3, x._4, x._5, x._6, x._7))

    val PMIRatings2 = polarityList.map(x =>
      (x._1, x._2, x._3,
        computePMI(x._4, positiveFrequencyL, x._4 + x._5, sizeL) -
          computePMI(x._5, negativeFrequencyL, x._4 + x._5, sizeL))).
      filter(_._3 > 50).sortBy(_._3).reverse

    val fw1 = new FileWriter("src/main/resources/staging/sentimetRatings_1.csv")
    val writer1 = CSVWriter.open(fw1)
    try writer1.writeAll(PMIRatings) finally fw1.close()

    val fw2 = new FileWriter("src/main/resources/staging/sentimetRatings_2.csv")
    val writer2 = CSVWriter.open(fw2)
    try writer2.writeAll(PMIRatings2.map(x => List(x._1, x._2, x._3, x._4))) finally fw2.close()
  }

}
