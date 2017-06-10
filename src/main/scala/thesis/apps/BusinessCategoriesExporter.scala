package com.reviews

import com.mongodb.{BasicDBObject, DBObject}

import scala.collection.mutable.ListBuffer
import java.io._
import java.util

import com.mongodb.BasicDBObject
import com.mongodb.casbah.Imports._

import scala.util.Try


object ExportBusinessCategories extends App {

  val db = DBConnection.getConnection
  val collection = db.getCollection("business")

  val query = new BasicDBObject()
  val fields = new BasicDBObject()
  query.put("categories", new BasicDBObject("$exists", true))
  query.put("categories", new BasicDBObject("$ne", null))
  fields.put("categories", 1)

  var tagsList = ListBuffer[String]()
  var counter = 0

  val queryResult = collection.find(query, fields)

  while(queryResult.hasNext) {
    val dbObject = Try(queryResult.next()).toOption
    dbObject match {
      case Some(dbObject) => {
        val categories = Try(dbObject.as[List[String]]("categories")).toOption
        categories match {
          case Some(categories) => {
            tagsList += categories.mkString(" ")
            counter += 1
            println(counter)
          }
          case _ => println("No categories could be parsed")
        }
      }
      case _ => println("Could not get pointer. Skipping...")
    }
  }

    val fw = new FileWriter("src/main/resources/business_tags.txt", true)
    val newLine = System.getProperty("line.separator")
    try tagsList.foreach(x => fw.write(x + newLine)) finally fw.close()
    println("Wrote a new file")

}

