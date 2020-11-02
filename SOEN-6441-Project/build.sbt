name := """play-java-hello-world-tutorial"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.1"

libraryDependencies += guice
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.2"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.7"
