name := "akka-streams-alpakka-maprdb"

version := "0.1"

scalaVersion := "2.12.1"

resolvers += "MapR Releases" at "http://repository.mapr.com/maven/"

resolvers += "JBoss" at "https://repository.jboss.org/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.6.3",

  "com.mapr.ojai" % "mapr-ojai-driver" % "6.1.0-mapr" % "provided",
  "org.apache.hadoop" % "hadoop-client" % "2.7.0-mapr-1808",
  "org.ojai" % "ojai" % "3.0-mapr-1808",
  "org.ojai" % "ojai-scala" % "3.0-mapr-1808",

  "com.mapr.db" % "maprdb" % "6.1.0-mapr",
  "xerces" % "xercesImpl" % "2.11.0",
   "com.github.anicolaspp" % "ojai-testing_2.12" % "1.0.12",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,,
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.6.3" % Test
)
  .map(_.exclude("org.slf4j", "slf4j-log4j12"))
