ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "RedisLite"
  )

val PekkoVersion = "1.0.2"
libraryDependencies += "org.apache.pekko" %% "pekko-stream" % PekkoVersion