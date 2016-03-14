organization := "com.cloudera"

version := "0.1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

resolvers += "Cloudera Hadoop Repo" at "https://repository.cloudera.com/content/repositories/releases"
resolvers += "Cloudera Repos" at "http://repository.cloudera.com/cloudera/cloudera-repos/"
resolvers += "Concurrent Jars" at "http://conjars.org/repo"

libraryDependencies ++= Seq(
  "org.apache.flume" % "flume-ng-core" % "1.5.0" % "provided",
  "org.apache.flume" % "flume-ng-sdk" % "1.5.0" % "provided",
  "org.apache.flume" % "flume-ng-tests" % "1.5.0" % "test",

  "org.apache.avro" % "avro" % "1.7.6-cdh5.4.7" % "provided",

  "org.apache.kafka" %% "kafka" % "0.8.2.2" % "provided"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple"),

  "org.apache.spark" %% "spark-streaming" % "1.3.0-cdh5.4.7"  % "provided",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.3.0-cdh5.4.7" % "provided" ,
  "org.apache.spark" %% "spark-sql" % "1.3.0-cdh5.4.7" % "provided",
  "org.apache.spark" %% "spark-hive" % "1.3.0-cdh5.4.7" % "provided",

  "com.beust" % "jcommander" % "1.48",

  "junit" % "junit" % "4.12" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.apache.curator" % "curator-test" % "3.1.0" % "test",
  "org.apache.kafka" %% "kafka" % "0.8.2.2" % "provided" classifier "test"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple")
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
