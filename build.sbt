name := "yelp_reviews"

version := "1.1"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.8.2",
  "joda-time" % "joda-time" % "2.9.7",
  "org.clulab" % "processors-corenlp_2.11" % "6.0.5",
  "org.clulab" % "processors-main_2.11" % "6.0.5",
  "org.clulab" % "processors-models_2.11" % "6.0.5",
  "com.jcraft" % "jsch" % "0.1.53",
  "com.typesafe.play" % "play-json_2.11" % "2.6.0-M7",
  "com.github.tototoshi" % "scala-csv_2.11" % "1.3.4",
  "com.optimaize.languagedetector" % "language-detector" % "0.5"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
        