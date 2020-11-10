name := """SOEN-6441-Project"""
organization := "com.example"

version := "1.0-SNAPSHOT"




lazy val root = (project.in(file("."))).enablePlugins(PlayJava)


scalaVersion := "2.13.1"


libraryDependencies += guice
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.2"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.7"
// https://mvnrepository.com/artifact/org.mockito/mockito-core
libraryDependencies += "org.mockito" % "mockito-core" % "3.6.0" % Test
// https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-api" % "5.7.0" % Test
// https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
libraryDependencies += "org.mockito" % "mockito-junit-jupiter" % "3.6.0" % Test
// https://mvnrepository.com/artifact/org.junit.platform/junit-platform-launcher
libraryDependencies += "org.junit.platform" % "junit-platform-launcher" % "1.7.0" % Test
// https://mvnrepository.com/artifact/org.junit.platform/junit-platform-runner
libraryDependencies += "org.junit.platform" % "junit-platform-runner" % "1.7.0" % Test



