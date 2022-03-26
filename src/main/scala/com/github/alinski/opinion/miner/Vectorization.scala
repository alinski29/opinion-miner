package com.github.alinski.opinion.miner

import Store.wordVectors

trait Vectorization {
  val vector: List[Double]
  def similarity(another: AbstractWord): Double =
    Vectorization.similarity(this.vector, another.vector)
  def closestNeighbors(n: Int = 10, threshold: Double = 0.65): List[(AbstractWord, Double)]
}

object Vectorization {
  def similarity(vectorA: List[Double], vectorB: List[Double]): Double = {
    if (vectorA.nonEmpty && vectorB.nonEmpty) {
      cosineSimilarity(vectorA, vectorB)
    } else {
      0.0
    }
  }
  private def dotProduct(x: List[Double], y: List[Double]) = (for ((a, b) <- x zip y) yield a * b) sum
  private def magnitude(x: List[Double]) = math.sqrt(x map (i => i * i) sum)
  private def cosineSimilarity(x: List[Double], y: List[Double]): Double = {
    require(x.size == y.size)
    dotProduct(x, y) / (magnitude(x) * magnitude(y))
  }

  def closestNeighbors(word: String, n: Int = 10, threshold: Double = 0.65): List[(String, Double)] = {
    val wordVec = wordVectors.getOrElse(word, List())
    if (wordVec.nonEmpty) {
      wordVectors.keys
        .map(w => (w, similarity(wordVec, wordVectors.getOrElse(w, List()))))
        .filter(x => x._2 > threshold && x._1 != word)
        .toList
        .sortBy(_._2)
        .reverse
        .take(n)
    } else {
      List()
    }
  }
  def topicSimilarity(
      word: String,
      topics: Map[String, List[String]]
  ): Map[String, Double] = {
    val weights = Seq(0.5, 0.3, 0.2)
    val wordVec = wordVectors.getOrElse(word, List())
    topics
      .map { case (topic, words) => (topic, words.map(x => similarity(wordVec, wordVectors.getOrElse(x, List())))) }
      .map { case (topic, distances) =>
        val closestNeighbors = distances.filterNot(_.equals(0.0)).sorted.reverse.take(weights.length)
        topic -> closestNeighbors.zip(weights).map(x => x._1 * x._2).sum
      }
  }
}
