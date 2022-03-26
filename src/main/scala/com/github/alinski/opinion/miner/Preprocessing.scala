package com.github.alinski.opinion.miner

trait Preprocessing {
  val lemma: String
  val pos: Option[String]
}

object Preprocessing {
  def defaultPos(word: String): String = "JJ"
  def defaultLemma(word: String): String = word.toLowerCase.trim
}
