import sbt._

object MyBuild extends Build {
  lazy val root = Project("root", file(".")) dependsOn(phrasesProject)
  lazy val phrasesProject = RootProject(uri("https://github.com/alinski29/phrases.git"))
}
