ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

libraryDependencies += "com.softwaremill.ox" %% "core" % "0.0.9"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

enablePlugins(JmhPlugin)

val jmhVersion = "1.36"

libraryDependencies += "org.openjdk.jmh" % "jmh-core" % jmhVersion
// GPLv2 with classpath exception
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode" % jmhVersion
// GPLv2 with classpath exception
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion
// GPLv2 with classpath exception
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-asm" % jmhVersion // GPLv2 with classpath exception


lazy val root = (project in file("."))
  .settings(
    name := "ox-playground"
  )
