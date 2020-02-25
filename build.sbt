name := "akka-streams-alpakka-maprdb"

version := "0.1"

scalaVersion := "2.12.1"

resolvers += "MapR Releases" at "http://repository.mapr.com/maven/"

resolvers += "JBoss" at "https://repository.jboss.org/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.17",

  "com.mapr.ojai" % "mapr-ojai-driver" % "6.1.0-mapr"
)
