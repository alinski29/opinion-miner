package com.github.alinski.opinion.miner

import OpinionMiner._

object ExampleApp extends App {

  val phrase =
    """Great food and atmosphere. Plenty of TVs to watch the games. The chef and his partners just opened this great location.
      | Pumpkin Soup with pumpkin oil and croutons is such a great start to the Fall season. Wood fired oven pumping out flatbreads.
      | Sweet Potato gnocchi made in house with roasted corn and gorgonzola crema is unbelievable.
      | Very impressive selection of beer handles and delicious cocktails. Amazing view of the sunset as well. Can't wait to return.
      |""".stripMargin

  val opinions = OpinionMiner(phrase)
  opinions.foreach(println)
  println("\n")
  opinions.groupByTopic.foreach(println)

}
