package mongo

import salat._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.TypeImports
import org.joda.time.format.DateTimeFormat
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.Imports._
import org.joda.time.LocalDate


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


