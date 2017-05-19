package com.reviews

import com.mongodb.{BasicDBObject, DBObject}
import com.mongodb.casbah.Imports.ObjectId
import org.joda.time.LocalDate

case class Configuration(database: String,
                         collection: String = "yelp",
                         host: String,
                         port: Int = 27017,
                         local_host: String = "127.0.0.1",
                         local_port: Int = 27017,
                         ssh_user: String,
                         ssh_port: Int,
                         ssh_password: String)

case class Sentence(_id: ObjectId,
                   length: Int,
                   count_words: Int,
                   sentiment: String,
                   Phrases: List[String])

case class Phrase(rev_id: String,
                  sentence_no: Int,
                  user_id: String,
                  business_id: String,
                  aspect: String,
                  sentiment: String,
                  polarity: String,
                  rating: Int) {

  def toDBObject(): DBObject = {
    val dbObject = new BasicDBObject()
    dbObject.put("rev_id", this.rev_id)
    dbObject.put("user_id", this.user_id)
    dbObject.put("business_id", this.business_id)
    dbObject.put("sentence_no", this.sentence_no)
    dbObject.put("aspect", this.aspect)
    dbObject.put("sentiment", this.sentiment)
    dbObject.put("polarity", this.polarity)
    dbObject.put("rating", this.rating)
    return dbObject
  }

}

case class Review(_id: ObjectId,
                  user_id : String,
                  review_id: String,
                  business_id: String,
                  date: org.joda.time.LocalDate,
                  votes: Map[String, Int],
                  starts: Int,
                  text: String)

case class Business(_id: ObjectId,
                   business_id: String,
                   name: String,
                   stars: Double)

case class User(_id: ObjectId,
               votes: Map[String, Int],
               user_id: String)

case class Tip(_id: ObjectId,
                user_id: String,
                business_id: String,
                date: LocalDate,
                text: String)



