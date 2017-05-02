package mongo

import salat._
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.Imports._
import org.joda.time.format.DateTimeFormat

import scala.util.Try

object Test extends App {

  val con = new MongoFactory().db
  val reviews = con("review")

  val result = reviews.find(MongoDBObject()).limit(100)
  implicit val datePattern = DateTimeFormat.forPattern("YYYY-mm-dd")


  def parseReview(collection:MongoCollection, name: String, field: String, value: String, limit: Option[Int]) = {
    name match {
      case "review" => {
        limit match {
          case Some(limit) => val QueryResult = collection.find(MongoDBObject(field -> value)).limit(limit)
          case None => val queryResult = collection.find(MongoDBObject(field -> value))
        }
      }

    }
  }

    def dbObjToReview(dbObj: MongoDBObject) =  {
        Review(
          dbObj.as[ObjectId]("_id"),
          dbObj.as[String]("user_id"),
          dbObj.as[String]("review_id"),
          dbObj.as[String]("business_id"),
          datePattern.parseDateTime(dbObj.as[String]("date")).toLocalDate,
          dbObj.as[Map[String, Int]]("votes"),
          dbObj.as[Int]("stars"),
          dbObj.as[String]("text"))
    }





    //for (res <- result)  println(dbObjToClass(res))
  val test = parseReview(reviews, "review", "", "", limit = Option(100))
  println(test)



}