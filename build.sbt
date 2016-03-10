organization := "com.cloudera"

version := "0.1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

resolvers += "Cloudera Hadoop Repo" at "https://repository.cloudera.com/content/repositories/releases"
resolvers += "Cloudera Repos" at "http://repository.cloudera.com/cloudera/cloudera-repos/"
resolvers += "Concurrent Jars" at "http://conjars.org/repo"

lazy val currentDependencies = {
  val sparkV = "1.3.0-cdh5.4.7"
  val kafkaV = "0.8.2.2"
  val flumeV = "1.5.0"

  Seq(
    "org.apache.flume" % "flume-ng-core" % "1.5.0" % "provided",
    "org.apache.flume" % "flume-ng-sdk" % "1.5.0" % "provided",
    "org.apache.flume" % "flume-ng-tests" % "1.5.0" % "test",

    "org.apache.kafka" %% "kafka" % "0.8.2.2" % "provided"
      exclude("javax.jms", "jms")
      exclude("com.sun.jdmk", "jmxtools")
      exclude("com.sun.jmx", "jmxri")
      exclude("org.slf4j", "slf4j-simple"),

    "org.apache.spark" %% "spark-streaming" % sparkV % "provided",
    "org.apache.spark" %% "spark-streaming-kafka" % sparkV % "provided",
    "org.apache.spark" %% "spark-sql" % sparkV % "provided",
    "org.apache.spark" %% "spark-hive" % sparkV % "provided"
  )
}

lazy val testDependencies = {
  val junitV = "4.12"
  val scalatestV = "2.2.6"
  val curatorV = "3.1.0"
  val kafkaV = "0.8.2.2"

  Seq(
    "junit" % "junit" % junitV % "test",
    "org.scalatest" %% "scalatest" % scalatestV % "test",
    "org.apache.curator" % "curator-test" % curatorV % "test",
    "org.apache.kafka" %% "kafka" % kafkaV % "test" classifier "test"
      exclude("javax.jms", "jms")
      exclude("com.sun.jdmk", "jmxtools")
      exclude("com.sun.jmx", "jmxri")
      exclude("org.slf4j", "slf4j-simple")
  )
}

libraryDependencies ++= (
  currentDependencies ++ testDependencies
)

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.first
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}
