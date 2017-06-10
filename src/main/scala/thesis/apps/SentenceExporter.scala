package thesis.apps

import java.io._

import com.mongodb.{BasicDBObject, DBObject}
import com.mongodb.casbah.Imports._
import thesis.operations.DBOperations.DBConnection

import scala.collection.mutable.ListBuffer
import scala.util.Try

/*
Gets all the English sentences from the database and exports them to a .txt file
containing sentence per each line. This file is used to train a word2vec model
 */

object SentenceExporter extends App {

  val db = DBConnection.getConnection
  val review = db.getCollection("review")
  val fw = new FileWriter("src/main/resources/models/input/sentences.txt", true)

  val query = new BasicDBObject()
  query.put("sentences", new BasicDBObject("$exists", true))
  query.put("lang", "en")

  val fields = new BasicDBObject()
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

    //Dump File from time to time so we don't keep all sentences (>20 milion) in memory
    if (counter >= 300000) {
      sentenceList.foreach(x => fw.write(x))
      counter = 0
      sentenceList = ListBuffer[String]()
    }

  }

  sentenceList.foreach(x => fw.write(x))
  fw.close()

}