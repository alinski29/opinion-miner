package thesis.apps

import java.io._
import scala.collection.mutable.ListBuffer
import scala.util.Try
import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._
import thesis.operations.DBOperations.DBConnection

/*
Reads business categories from yelp.business, puts them in a string delimited by " " and writes them to a .txt file
The .txt file serves as an input for training a topic model or a word embedings model
 */

object BusinessCategoriesExporter extends App {

  val db = DBConnection.getConnection
  val collection = db.getCollection("business")
  val fw = new FileWriter("src/main/resources/models/input/business_tags.txt", true)

  val query = new BasicDBObject()
  query.put("categories", new BasicDBObject("$exists", true))
  query.put("categories", new BasicDBObject("$ne", null))
  val fields = new BasicDBObject()
  fields.put("categories", 1)

  var tagsList = ListBuffer[String]()
  var counter = 0

  val queryResult = collection.find(query, fields)
  while(queryResult.hasNext) {
    val dbObject = Try(queryResult.next()).toOption
    dbObject match {
      case Some(dbResult) => {
        val categories = Try(dbResult.as[List[String]]("categories")).toOption
        categories match {
          case Some(values) => {
            tagsList += values.mkString(" ")
            counter += 1
            println(counter)
          }
          case _ => println("No categories could be parsed")
        }
      }
      case _ => println("Could not get pointer. Skipping...")
    }
  }

    val newLine = System.getProperty("line.separator")
    try tagsList.foreach(x => fw.write(x + newLine)) finally fw.close()

}

