name := "yelp_reviews"

version := "1.0"

scalaVersion := "2.11.10"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.8.2",
  "joda-time" % "joda-time" % "2.9.7",
  "org.clulab" % "processors-corenlp_2.11" % "6.0.5",
  "org.clulab" % "processors-main_2.11" % "6.0.5",
  "org.clulab" % "processors-models_2.11" % "6.0.5",
  "com.jcraft" % "jsch" % "0.1.53",
  "com.typesafe.play" % "play-json_2.11" % "2.6.0-M7"
)
        