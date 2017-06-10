package thesis.structure

import com.mongodb.{BasicDBObject, DBObject}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.util.Try

object Wrappers {

  abstract class Phrase {
    val rev_id: String
    val sentence_no: Int
    val user_id: String
    val business_id: String
    val aspect: String
    val sentiment: String
    val polarity: String
    val rating: Int

    def toList(): List[Any] = {
      this.getClass.getDeclaredFields.map { f =>
        f.setAccessible(true)
        Try(f.get(this)).toOption.getOrElse("null")
      }.toList
    }

    def toDBObject(): DBObject = {
      val dbObject = new BasicDBObject()
      this.getClass.getDeclaredFields.map { f =>
        f.setAccessible(true)
        val value = Try(f.get(this)).toOption.getOrElse("null")
        if (value != "null") dbObject.put(f.getName, value)
      }
      dbObject
    }
  }

  case class ExtendedPhrase(
       phrase_id: String,
       rev_id: String,
       sentence_no: Int,
       user_id: String,
       business_id: String,
       date: String,
       lang: String,
       aspect: String,
       aspect_topic: String,
       aspect_topic_secondary: String,
       aspect_pos: String,
       sentiment: String,
       sentiment_pos: String,
       sentiment_score: Double,
       sentiment_score2: Double,
       dependency_type: String,
       polarity: String,
       rating: Int) extends Phrase

  case class Aspect(
       word: String,
       topic: String,
       topic_secondary: String,
       wordVector: List[Double])

  case class Sentiment(
      word: String,
      frequency: Int,
      avg_rating: Double,
      composite_rating: Double,
      avg_rating_pos: Double,
      avg_rating_neg: Double,
      wordVector: List[Double])

  implicit val ExtendedPhraseWriter: Writes[ExtendedPhrase] = (
      (JsPath \ "phrase_id").write[String] and
      (JsPath \ "rev_id").write[String] and
      (JsPath \ "sentence_no").write[Int] and
      (JsPath \ "user_id").write[String] and
      (JsPath \ "business_id").write[String] and
      (JsPath \ "date").write[String] and
      (JsPath \ "lang").write[String] and
      (JsPath \ "aspect").write[String] and
      (JsPath \ "aspect_topic").write[String] and
      (JsPath \ "aspect_topic_secondary").write[String] and
      (JsPath \ "aspect_pos").write[String] and
      (JsPath \ "sentiment").write[String] and
      (JsPath \ "sentiment_pos").write[String] and
      (JsPath \ "sentiment_score").write[Double] and
      (JsPath \ "sentiment_score2").write[Double] and
      (JsPath \ "dependency_type").write[String] and
      (JsPath \ "polarity").write[String] and
      (JsPath \ "rating").write[Int]
    ) (unlift(ExtendedPhrase.unapply))

  implicit val ExtendedPhraseReader: Reads[ExtendedPhrase] = (
     (JsPath \ "phrase_id").read[String] and
      (JsPath \ "rev_id").read[String] and
      (JsPath \ "sentence_no").read[Int] and
      (JsPath \ "user_id").read[String] and
      (JsPath \ "business_id").read[String] and
      (JsPath \ "date").read[String] and
      (JsPath \ "lang").read[String] and
      (JsPath \ "aspect").read[String] and
      (JsPath \ "aspect_topic").read[String] and
      (JsPath \ "aspect_topic_secondary").read[String] and
      (JsPath \ "aspect_pos").read[String] and
      (JsPath \ "sentiment").read[String] and
      (JsPath \ "sentiment_pos").read[String] and
      (JsPath \ "sentiment_score").read[Double] and
      (JsPath \ "sentiment_score2").read[Double] and
      (JsPath \ "dependency_type").read[String] and
      (JsPath \ "polarity").read[String] and
      (JsPath \ "rating").read[Int]
    ) (ExtendedPhrase.apply _)

}





