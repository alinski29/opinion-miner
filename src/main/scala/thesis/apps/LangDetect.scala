package thesis.apps

import thesis.operations.DBOperations.{DBConnection, updateCollection}
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoClient
import com.optimaize.langdetect.LanguageDetectorBuilder
import com.optimaize.langdetect.ngram.NgramExtractors
import com.optimaize.langdetect.profiles.LanguageProfileReader
import com.optimaize.langdetect.text.CommonTextObjectFactories
import scala.util.Try

/*
Uses "com.optimaize.languagedetector" to detect language of each review.
If language is detected, writes to database collection the field lang for the respective review
It is recommended to build this app and run it on the server for better performance
 */

object LangDetect {

  def main(args: Array[String]) {

    val location = args(0)
    val db = location match {
      case "server" => DBConnection.getConnection
      case _ => MongoClient(host = "127.0.0.1", port = 27017)("yelp")
    }
    val review = db.getCollection("review")

    val query = new BasicDBObject()
    query.put("lang", new BasicDBObject("$exists", false))
    query.put("text", new BasicDBObject("$exists", true))
    val fields = new BasicDBObject()
    List("_id", "text").map(field => fields.put(field, 1))
    val queryResult = review.find(query, fields)

    //Language detector settings
    val languageProfiles = new LanguageProfileReader().readAllBuiltIn
    val languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
      .withProfiles(languageProfiles).build()
    val textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText

    //Our counters
    var docProc = 0
    var docCount = queryResult.count
    var reviewList = scala.collection.mutable.HashMap[ObjectId, String]()

    while (queryResult.hasNext) {
      val dbObject = Try(queryResult.next()).toOption
      dbObject match {
        case Some(dbObject) => {
          val mongoId = dbObject.as[ObjectId]("_id")
          val text = dbObject.as[String]("text")
          val textObject = textObjectFactory.forText(text)
          val lang = Try(languageDetector.detect(textObject).get()).toOption.getOrElse("not detected").toString
          docProc += 1
          updateCollection(mongoId, "lang", lang, review)
          println(s"Processed: $docProc / $docCount")
        }
        case _ => println("Cursor not found. Moving forward")
      }
    }
  }

}