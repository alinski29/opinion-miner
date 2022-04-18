package com.github.alinski.opinion.miner

import scala.io.Source
import java.io.File

object Store {

  lazy val wordVectors: Map[String, Vector[Double]] = {
    (for (line <- open("/words.vec").read if line.length > 100) yield {
      val splits = line.split(" ")
      (splits.head, splits.tail.map(_.toDouble).toVector)
    }).toMap
  }

  lazy val sentimentScores: Map[String, Double] = {
    (for (line <- open("/sentimentScores.txt").read) yield {
      val splits = line.split(",")
      (splits.head, splits.last.toDouble)
    }).toMap
  }

  lazy val mainTopics: Map[String, List[String]] = readTopics("/topicsMain.txt")
  lazy val secondaryTopics: Map[String, List[String]] = readTopics("/topicsSecondary.txt")

  private def readTopics(path: String): Map[String, List[String]] = {
    (for (line <- open(path).read) yield {
      val splits = line.split(",").map(_.trim).map(_.toLowerCase)
      (splits.head, splits.tail.toList)
    }).toMap
  }
  def open(path: String) = new File(getClass.getResource(path).getPath)
  implicit class FileOpenOps(file: File) {
    def read: Iterator[String] = {
      val src = Source.fromFile(file)
      val lines = src.getLines()
      //      src.close()
      lines
    }
  }

}
