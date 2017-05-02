name := "yelp_reviews"

version := "1.0"

scalaVersion := "2.11.10"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.8.2",
  "joda-time" % "joda-time" % "2.9.7",
  "com.github.salat" % "salat_2.11" % "1.10.0",
  "org.clulab" % "processors-corenlp_2.11" % "6.0.5",
  "org.clulab" % "processors-main_2.11" % "6.0.5",
  "org.clulab" % "processors-models_2.11" % "6.0.5"
)
        