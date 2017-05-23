package com.reviews

import com.mongodb.{BasicDBObject, DBObject}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import java.io._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.parallel.mutable.ParSeq
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import com.reviews.PrepareTM.finalPhrases


object SentenceExporter extends App {

  val db = DBConnection.getConnection
  val review = db.getCollection("review")

  val query = new BasicDBObject()
  val fields = new BasicDBObject()
  for (field <- List("review_id","sentences")) query.put(field, new BasicDBObject("$exists", true))
  for (field <- List("review_id","sentences")) fields.put(field, 1)

  val queryResult = review.find(query, fields)
  var sentenceList = ListBuffer[String]()
  var counter = 0
  while(queryResult.hasNext) {
    val dbObject = queryResult.next()
    val mongoId = dbObject.as[ObjectId]("_id")
    val sentences = dbObject.as[List[DBObject]]("sentences")

    for (sentenceObj <- sentences) {
      val tokens = sentenceObj.as[List[String]]("tokens")
      val sentenceString = tokens.map(x => x.split(", ").dropRight(1).mkString(" ")).mkString(" ").
        replace("-RRB-","").replace("-LRB-","").trim()
      sentenceList += sentenceString
      counter += 1
    }

    println(counter)
    if (counter > 100000) {
    val fw = new FileWriter("sentences.txt", true)
    try {
      sentenceList.foreach(x => fw.write(x))
    }
    finally fw.close()
    println("Wrote a new file")
    counter = 0
    sentenceList = ListBuffer[String]()
    }
  }

}