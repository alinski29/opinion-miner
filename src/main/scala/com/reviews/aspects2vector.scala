package com.reviews

import java.io.FileWriter
import org.clulab.processors.fastnlp.FastNLPProcessor

object Aspects2Vector extends App {

  type Aspect = (String, List[Double])
  val aspectsFile = io.Source.fromFile("src/main/resources/aspects_unique.txt").getLines().toList.map(_.trim)
  val allVectors = io.Source.fromFile("models/ft_sentences.vec").getLines().toList
  val allWords = scala.collection.mutable.ListBuffer[Aspect]()

  for (line <- allVectors) {
    val splits = line.split(" ").toList
    allWords += Tuple2(splits.head, splits.tail.map(_.toDouble))
  }

  val filteredVector = allWords.filter(x => aspectsFile.contains(x._1))

  val proc = new FastNLPProcessor
  val lemmaVector = scala.collection.mutable.ListBuffer[Aspect]()
  var lemmaCount = 0
  for (word <- filteredVector) {
    val doc = proc.mkDocument(word._1)
    proc.tagPartsOfSpeech(doc)
    proc.lemmatize(doc)
    val lemma = doc.sentences.map(_.lemmas.get).map(x => x.mkString(" ")).mkString("")
    doc.clear()
    lemmaVector += Tuple2(lemma, word._2)
    lemmaCount += 1
    println(s"Lemmatizing count: $lemmaCount")
  }

  val finalVector = lemmaVector.groupBy(_._1).map(_._2.head)
  finalVector.take(10).foreach(println)

  val fw = new FileWriter("src/main/resources/aspect_vectors", true)
  try {
    finalVector.map(x => x._1 + ", " + x._2.mkString(" ")).foreach(v => fw.write(v))
  }
  finally fw.close()

}