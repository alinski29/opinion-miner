package com.reviews

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import java.io._

case class TMPhrase(rev_id: String,
                   sentence_id: String,
                   user_id: String,
                   aspect: String,
                   sentiment: String,
                   polarity: String)

object PrepareTM extends App {

  val bufferedSource = io.Source.fromFile("src/main/resources/yelp.phrases.csv")

  val phraseList = mutable.HashMap[String,ListBuffer[String]]()
  for (line <- bufferedSource.getLines) {
    val cols = line.split(",").map(_.trim)
    val sentenceId = cols(3) + cols(4)
    val phrase = TMPhrase(cols(3), sentenceId, cols(6), cols(1), cols(5), cols(2))
    val existingPhrases: ListBuffer[String] = phraseList.getOrElse(sentenceId, ListBuffer[String]())
    if (existingPhrases.isEmpty) {
      phraseList += ((sentenceId, existingPhrases += phrase.aspect))
    } else {
      phraseList.update(sentenceId, existingPhrases += phrase.aspect)
    }
  }
  println("Finished phrase list")
  val finalPhrases = phraseList.map(x => (x._1, x._2.mkString(", ")))
  finalPhrases.take(1000).foreach(println)

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  printToFile(new File("aspects.txt")) {
    p => finalPhrases.foreach(p.println)
  }


}