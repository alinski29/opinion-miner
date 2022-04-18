package com.github.alinski.opinion.miner

import Store.{sentimentScores, wordVectors}

case class Sentiment(word: String, lemma: String, pos: Option[String])
    extends AbstractWord
    with Vectorization {
  lazy val vector: Vector[Double] = wordVectors.getOrElse(lemma, Vector())
  override def closestNeighbors(n: Int = 10, threshold: Double = 0.65): List[(Sentiment, Double)] =
    Vectorization.closestNeighbors(this.lemma, n, threshold).map(x => (Sentiment(x._1), x._2))
  lazy val score: Option[Double] = {
    sentimentScores.get(lemma) match {
      case Some(rating) => Some(rating)
      case _            => Sentiment.tryAssignScore(lemma)
    }
  }
  override def toString: String = {
    score match {
      case Some(value) => f"Sentiment($word, $value%1.2f)"
      case _           => s"Sentiment($word, ?)"
    }
  }
}

object Sentiment {
  def apply(word: String, lemma: Option[String] = None, pos: Option[String] = None): Sentiment = {
    (pos, lemma) match {
      case (Some(aLemma), Some(aPos)) => new Sentiment(word, aLemma, Some(aPos))
      case (None, Some(aPos))         => new Sentiment(word, Preprocessing.defaultLemma(word), Some(aPos))
      case (Some(aLemma), None)       => new Sentiment(word, aLemma, None)
      case _                          => new Sentiment(word, Preprocessing.defaultLemma(word), None)
    }
  }
  def tryAssignScore(word: String): Option[Double] = {
    val neighbors = Vectorization
      .closestNeighbors(word, threshold = 0.70)
      .filter(x => sentimentScores.contains(x._1))
    neighbors.length match {
      case n if n >= 3 => Some(neighbors.take(3).map(_._1).map(sentimentScores(_)).sum / 3)
      case _           => None
    }
  }
}
