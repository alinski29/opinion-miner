package thesis.apps

import java.io.FileWriter

import com.github.tototoshi.csv.CSVWriter
import com.mongodb.{BasicDBObject, DBObject}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoClient, MongoDB}
import thesis.operations.DBOperations.DBConnection
import thesis.structure.Wrappers.ExtendedPhrase

import scala.collection.immutable._
import scala.collection.mutable.ListBuffer
import scala.util.Try

/*
Reads Phrases from database phrases collection, extracts POS and dependencies from the database and
makes a CSV file with all the phrases. Recommended to run it on the server
*/

object Phrases2CSVExporter {

  def main(args: Array[String]) {

    val location = Try(args(0)).getOrElse("server")
    val insertBatchSize = Try(args(1).toInt).getOrElse(10000)
    val outputLocation = Try(args(2)).getOrElse("src/main/resources/staging/phrases.csv")

    val db = location match {
      case "server" => DBConnection.getConnection
      case _ => MongoClient(host = "127.0.0.1", port = 27017)("yelp")
    }
    val review = db.getCollection("review")
    val phrases = db.getCollection("phrases")

    val query = new BasicDBObject()
    val fields = new BasicDBObject()
    List("_id,", "review_id", "user_id", "business_id", "sentences", "stars", "date", "lang").map(f => fields.put(f, 1))
    var docProc = 0
    var batchCount = 0

    val queryResult = review.find(query, fields)
    val docCount = queryResult.count()
    var csvList = ListBuffer[List[Any]]()
    val fw = new FileWriter(outputLocation, true)
    val writer = CSVWriter.open(fw)

    while (queryResult.hasNext) {
      val dbObject = Try(queryResult.next()).toOption
      dbObject match {
        case Some(dbResult) => {
          val tryLang = Try(dbResult.as[String]("lang")).toOption.getOrElse("")
          val trySentence = Try(dbResult.as[List[DBObject]]("sentences"))
          if (trySentence.isSuccess & tryLang == "en") {
            val mongoId = dbResult.as[ObjectId]("_id")
            val date = dbResult.as[String]("date")
            val rev_id = dbResult.as[String]("review_id")
            val business_id = dbResult.as[String]("business_id")
            val rating = dbResult.as[Int]("stars")
            val sentences = dbResult.as[List[DBObject]]("sentences")
            val user_id = dbResult.as[String]("user_id")
            val lang = dbResult.as[String]("lang")

            for (i <- sentences.seq.indices) {
              val polarity = Try(sentences(i).as[String]("sentiment")).toOption.getOrElse("")
              val opinions = Try(sentences(i).as[List[String]]("phrases")).toOption.getOrElse(List())
              val tokens = Try(sentences(i).as[List[String]]("tokens")).toOption.getOrElse(List())
              val dep = Try(sentences(i).as[List[String]]("dependencies")).toOption.getOrElse(List())
              val pos: Map[String, String] = extract_pos(tokens)
              val deps: Map[String, List[String]] = extract_deps(dep)

              for (o <- opinions.seq.indices if opinions.nonEmpty) {
                val aspSent = opinions(o).replace("-rrb-","na").split(" -> ")
                val aspect = aspSent(0).replace(",","").replace("-", "").trim
                val sentiment = aspSent(1).replace(",","").replace("-","").trim
                val phrase_id = rev_id + "s" + i.toString + "p" + o.toString
                val asp_pos = pos.getOrElse(aspect, "")
                val sent_pos = pos.getOrElse(sentiment, "")
                val dep_list = deps.filter(d => d._2.contains(aspect) && d._2.contains(sentiment)).map(_._1).toList
                val dep_type = dep_list match {
                  case List() => ""
                  case _ => dep_list.head
                }
                val newPhrase: List[Any] = List(
                  phrase_id, rev_id, i, user_id, business_id, date, lang, aspect,
                  asp_pos, sentiment, sent_pos, dep_type, polarity, rating)
                csvList += newPhrase
              }
            }
            batchCount += 1
            docProc += 1
          }
        }
        case _ => println("Cursor not found. Moving forward")
      }
      //Append to csv $insertBatchSize in order not to overload memory
      if (batchCount >= insertBatchSize) {
        writer.writeAll(csvList)
        println("Wrote " + csvList.length + " phrases")
        println(s"Progress: $docProc / $docCount")
        batchCount = 0
        csvList = ListBuffer[List[Any]]()
      }
    }

    writer.writeAll(csvList)
    fw.close()

  }

  private def extract_pos(tokens: List[String]): Map[String, String] = {
    tokens match {
      case List() => Map("string" -> "string")
      case x :: xs => {
        tokens.map(t => t.split(", ")).
          map(t => (t.dropRight(1).mkString(" ").trim, t.takeRight(1).mkString(" ").trim)).
          toMap }
    }
  }

  private def extract_deps(dep: List[String]): Map[String, List[String]] = {
    dep match {
      case List() => Map("string" -> List())
      case x :: xs => {
        dep.map(d => d.split("[(]")).
          map(d => (d(0).trim, d(1).split(", ", 2))).
          map(d => (d._1, List(d._2(0), d._2(1)))).
          map(d => (d._1.toLowerCase, d._2.map(s => s.split("-+[0-9]", 2)(0).toLowerCase))).
          toMap
      }
    }
  }

}

