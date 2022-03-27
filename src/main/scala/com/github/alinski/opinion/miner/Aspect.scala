package com.github.alinski.opinion.miner

import Store.{mainTopics, secondaryTopics, wordVectors}

trait Categorical {
  val topics: List[String]
//  def topicSimilarity(
//      topics: Map[String, List[String]],
//      vectors: Map[String, List[Double]]
//  ): Map[String, Double]
}

case class Aspect(word: String, lemma: String, pos: Option[String])
    extends AbstractWord
    with Vectorization
    with Categorical {
  lazy val vector: List[Double] = wordVectors.getOrElse(lemma, List())
  lazy val topics: List[String] = Aspect.assignTopics(lemma, 0.70)

  override def closestNeighbors(n: Int = 10, threshold: Double = 0.65): List[(Aspect, Double)] =
    Vectorization.closestNeighbors(this.lemma, n, threshold).map(x => (Aspect(x._1), x._2))

  override def toString: String = s"Aspect($word ∈ [${topics.mkString(", ")}])"

}

object Aspect {
  def apply(word: String, lemma: Option[String] = None, pos: Option[String] = None): Aspect = {
    (pos, lemma) match {
      case (Some(aLemma), Some(aPos)) => new Aspect(word, aLemma, Some(aPos))
      case (None, Some(aPos))         => new Aspect(word, Preprocessing.defaultLemma(word), Some(aPos))
      case (Some(aLemma), None)       => new Aspect(word, aLemma, None)
      case _                          => new Aspect(word, Preprocessing.defaultLemma(word), None)
    }
  }
  private def assignTopics(word: String, threshold: Double): List[String] = {
    val topics = word.split(" ").toList match {
      case a :: b :: Nil => {
        val aSim = Vectorization.topicSimilarity(a, mainTopics).filter(_._2 >= threshold).toList.sortBy(_._2).reverse
        val bSim = Vectorization.topicSimilarity(b, mainTopics).filter(_._2 >= threshold).toList.sortBy(_._2).reverse
        (aSim, bSim) match {
          case (x, y) if x.isEmpty && y.nonEmpty => y.map(_._1)
          case (x, y) if x.nonEmpty && y.isEmpty => x.map(_._1)
          case (x, y) if x.nonEmpty && y.nonEmpty =>
            if (x.head._1 == y.head._1)  {
              x.map(_._1)
            } else if (x.head._2 >= y.head._2){
              x.map(_._1)
            } else {
              y.map(_._1)
            }
        }
      }
      case a :: Nil => Aspect.sortTopics(Vectorization.topicSimilarity(a, mainTopics), threshold)
    }
    topics match {
      case x :: xs if x == "food" =>
        x :: Aspect.sortTopics(Vectorization.topicSimilarity(word, secondaryTopics), threshold)
      case x :: xs => List(x)
      case _       => List("other")
    }
  }
  private def sortTopics(dist: Map[String, Double], threshold: Double) =
    dist.toList.filter(_._2 > threshold).sortBy(_._2).reverse.map(_._1)
}
