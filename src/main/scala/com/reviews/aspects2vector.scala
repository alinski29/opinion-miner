package com.reviews

import java.io.FileWriter

import org.clulab.processors.fastnlp.FastNLPProcessor
import com.github.tototoshi.csv._

import scala.collection.mutable

object Aspects2Vector extends App {

  val aspectsFile = io.Source.fromFile("src/main/resources/aspects_unique.txt").getLines().toList.map(_.trim)
  val allVectors = io.Source.fromFile("models/ft_sentences.vec").getLines().toList
  val allWords = mutable.HashMap[String, List[Double]]().par

  for (line <- allVectors) {
    val splits = line.split(" ").toList
    allWords += splits.head -> splits.tail.map(_.toDouble)
  }

  val filteredVector = allWords.filter(x => aspectsFile.contains(x._1)).par

  val proc = new FastNLPProcessor
  val lemmaVector = mutable.HashMap[String, List[Double]]()
  var lemmaCount = 0
  for (word <- filteredVector) {
    val doc = proc.mkDocument(word._1)
    proc.tagPartsOfSpeech(doc)
    proc.lemmatize(doc)
    val lemma = doc.sentences.map(_.lemmas.get).map(x => x.mkString(" ")).mkString("")
    doc.clear()
    lemmaVector += lemma -> word._2
    lemmaCount += 1
    println(s"Lemmatizing count: $lemmaCount")
  }

  val finalVector = lemmaVector.par.groupBy(_._1).map(_._2.head).
    map(x => x._1 :: x._2).toList

  val fw = new FileWriter("src/main/resources/aspect_vectors.csv")
  val writer = CSVWriter.open(fw)
  try {
    writer.writeAll(finalVector)
  }
  finally fw.close()


}