package com.reviews

import com.mongodb.{BasicDBObject, DBObject}

import scala.collection.mutable.ListBuffer
import java.io._
import java.util

import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._

import scala.util.Try


object SentenceExporter extends App {

  val db = DBConnection.getConnection
  val review = db.getCollection("review")

  val query = new BasicDBObject()
  val fields = new BasicDBObject()
  query.put("sentences", new BasicDBObject("$exists", true))
  query.put("lang", "en")
  fields.put("sentences", 1)

  var sentenceList = ListBuffer[String]()
  var counter = 0

  val queryResult = review.find(query, fields)

  while(queryResult.hasNext) {
    val dbObject = Try(queryResult.next()).toOption
    dbObject match {
      case Some(dbObject) => {
        val sentences = dbObject.as[List[DBObject]]("sentences")
        for (sentenceObj <- sentences) {
          val tokens = sentenceObj.as[List[String]]("tokens")
          val sentenceString = tokens.map(x => x.split(", ").dropRight(1).mkString(" ")).mkString(" ").
            replace("-RRB-","").replace("-LRB-","").trim()
          sentenceList += sentenceString
          counter += 1
        }
        println(counter)
      }
      case _ => println("Could not get pointer. Skipping...")
    }

    if (counter >= 300000) {
      val fw = new FileWriter("src/main/resources/sentences.txt", true)
      try {
        sentenceList.foreach(x => fw.write(x))
      } finally fw.close()
      println("Wrote a new file")
      counter = 0
      sentenceList = ListBuffer[String]()
    }
  }

}