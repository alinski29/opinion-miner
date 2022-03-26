package com.github.alinski.opinion.miner

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import com.github.alinski.opinion.miner.OpinionMiner._

class OpinionMinerSpec extends AnyFlatSpec with Matchers {

  private val phrase = """
   | For large events, Driftwood handled it well. Knowledgeable and courteous staff and the food was
   | GREAT! We started with a delicious gazpacho instead of a salad, had the fish and short ribs. Delicious!
   | I'd also like to point out that their cocktail hour cheese plating buffet was spectacular. Huge blocks of
   | cheese, crackers, all very well displayed. Plus, my favorite thing about it was that they left the cheese
   | out all night and kept an eye on it, refilling as necessary. I can't tell you how great it was to be munching
   | on cheese all night!""".stripMargin

  val phrases: List[OpinionPhrase] = OpinionMiner(phrase)

  "The correct aspect, sentiment pairs" should "be identified" in {
    (phrases.map(_.aspect.word) intersect List("food", "cheese", "staff", "buffet", "gazpacho")).length shouldEqual 5
  }

  "Group by topic" should "work" in {
    val phrasesByTopic = phrases.groupByTopic
    phrasesByTopic.keys.toList.sorted shouldEqual List("food", "other", "service")
    phrasesByTopic("food").map(_.aspect.word) shouldEqual List("food", "food", "food", "cheese")
  }

}
