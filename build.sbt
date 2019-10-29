name := "play-gcp"

organization := "mrks"

version := "0.2"

scalaVersion := "2.12.6"

licenses += ("Apache-2.0", url("https://github.com/markussammallahti/play-gcp/blob/master/LICENSE"))

resolvers += Resolver.bintrayRepo("mrks", "maven")

libraryDependencies ++= Seq(
  "com.google.cloud" % "google-cloud-storage" % "1.98.0",
  "com.typesafe.play" %% "play" % "2.7.+" % Provided,
  "mrks" %% "scala-utils" % "0.1" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test,
  "com.google.cloud" % "google-cloud-nio" % "0.67.0-alpha" % Test
)
