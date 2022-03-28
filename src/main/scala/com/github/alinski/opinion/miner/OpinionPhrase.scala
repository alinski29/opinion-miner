package com.github.alinski.opinion.miner

import com.github.vladsandulescu.phrases.Extract
import scala.collection.JavaConversions.collectionAsScalaIterable

abstract class AbstractWord extends Preprocessing with Vectorization

case class OpinionPhrase(
    aspect: Aspect,
    sentiment: Sentiment
) {
  override def toString: String = s"($aspect, $sentiment)"
}

object OpinionMiner {
  private lazy val extract = new Extract()
  def apply(text: String): List[OpinionPhrase] = {
    extract
      .run(text)
      .toList
      .map(p =>
        OpinionPhrase(
          Aspect(p.head, Option(p.headTag), Option(p.headLemma).map(_.toLowerCase.trim)),
          Sentiment(p.modifier, Option(p.modifierTag), Option(p.modifierLemma).map(_.toLowerCase.trim))
        )
      )
  }

  implicit class OpinionPhrasesOps(phrases: List[OpinionPhrase]) {
    def groupByTopic: Map[String, List[OpinionPhrase]] = {
      phrases
        .map(phrase => (phrase.aspect.topics.head, phrase.aspect, phrase.sentiment))
        .groupBy(_._1)
        .mapValues(x => x.map(z => OpinionPhrase(z._2, z._3)))
    }
  }
}
