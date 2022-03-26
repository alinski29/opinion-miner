package com.github.alinski.opinion.miner

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class VectorOpsSpec extends AnyFlatSpec with Matchers {

  "Cosine similarity measures" should "be reasonable between related aspect words" in {
    Aspect("chicken").similarity(Aspect("beef")) > 0.7 shouldBe true
    Aspect("salt").similarity(Aspect("pepper")) > 0.7 shouldBe true
  }

  "Cosine similarity measures" should "be reasonable between related sentiment words" in {
    Sentiment("amazing").similarity(Sentiment("terrific")) > 0.8 shouldBe true
    Sentiment("terrible").similarity(Sentiment("worst")) > 0.8 shouldBe true
  }

  "Closest neighbors" should "be reasonable" in {
    val saltNeighbors = Aspect("salt").closestNeighbors().map(_._1.word)
    all(List("seasoning", "pepper").map(saltNeighbors.contains(_))) shouldBe true
    val falafelNeighbors = Aspect("falafel").closestNeighbors(50).map(_._1.word)
    all(List("shawarma", "hummus").map(falafelNeighbors.contains(_))) shouldBe true
  }

  "Topic similarity" should "be reasonable" in {
    Aspect("service").topics shouldEqual List("service")
    Aspect("waiter").topics shouldEqual List("service")
    Aspect("terrace").topics shouldEqual List("location")
    Aspect("stadium").topics shouldEqual List("location")
    Aspect("mood").topics shouldEqual List("ambiance")
    Aspect("vibe").topics shouldEqual List("ambiance")
    Aspect("falafel").topics shouldEqual List("food", "middle eastern")
    Aspect("crunchy").topics shouldEqual List("food", "taste")
    Aspect("burger").topics shouldEqual List("food", "fast food")
    Aspect("cheap").topics shouldEqual List("price")
    Aspect("expensive").topics shouldEqual List("price")
    Aspect("pricey").topics shouldEqual List("price")
    Aspect("bill").topics shouldEqual List("price")
  }

}
