package com.reviews

import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.immutable._
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.ParSeq
import scala.util.Try

object PutPhrases extends App {

  val db = DBConnection.getConnection
  val review = db.getCollection("review")
  val phrases = db.getCollection("phrases")

  val query = new BasicDBObject()
  val fields = new BasicDBObject()
  for (field <- List("review_id","sentences.sentiment")) query.put(field, new BasicDBObject("$exists", true))
  query.put("proc_phrase", new BasicDBObject("$exists", false))

  for (field <- List("_id,","review_id","user_id","business_id","sentences","stars")) fields.put(field, 1)
  val docCount = review.find(query, fields).count()
  var docProc = 0
  val batchsize = 500

  while(docProc <= docCount) {
    val skipValue = batchsize + (Math.random * ((30000 - batchsize) + 1)).asInstanceOf[Int]
    val queryResult: ParSeq[DBObject] = review.find(query, fields).skip(skipValue).limit(batchsize).toArray().toList.par
    val phraseList: ListBuffer[DBObject] = ListBuffer()
    val idsList: ListBuffer[ObjectId] = ListBuffer()

    for (dBObject <- queryResult) {
      val mongoId = dBObject.as[ObjectId]("_id")
      val rev_id = dBObject.as[String]("review_id")
      val business_id = dBObject.as[String]("business_id")
      val rating = dBObject.as[Int]("stars")
      val sentences = dBObject.as[List[DBObject]]("sentences").par
      val user_id = dBObject.as[String]("user_id")
      idsList += mongoId

      for (i <- 0 until sentences.length) {
        val polarity = Try(sentences(i).as[String]("sentiment")).toOption.getOrElse("")
        val opinions = sentences(i).as[List[String]]("phrases")
        if (polarity.nonEmpty && opinions.nonEmpty) {
          for (phrase <- opinions) {
            val aspSent = phrase.split(" -> ")
            val newPhrase = Phrase(rev_id, i, user_id, business_id, aspSent(0), aspSent(1), polarity, rating).toDBObject()
            phraseList += newPhrase
          }
        }
      }
      docProc += 1
    }

    if (phraseList.nonEmpty) {
      phrases.insert(phraseList.toList.asJava)
      println("Inserted " + phraseList.length + " phrases")
      idsList.map(id => updateStauts(id))
      println("Updated " + idsList.length + " ids" )
      println("Processed: " + docProc + " / " + docCount + " documents")
    }
  }

  def updateStauts(id: ObjectId) = {
    val q = new BasicDBObject("_id", id)
    val reviewObj = new BasicDBObject()
    reviewObj.put("proc_phrase", true)
    review.update(q, new BasicDBObject("$set", reviewObj))
  }

}

