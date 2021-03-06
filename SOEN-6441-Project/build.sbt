name := """SOEN-6441-Project"""
organization := "com.example"

version := "1.0-SNAPSHOT"



lazy val root = (project.in( file("."))).enablePlugins(PlayJava)

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
val AkkaVersion = "2.6.10"
libraryDependencies += "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor-testkit-typed
libraryDependencies += "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-serialization-jackson
libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion % Test
libraryDependencies += "org.scalatestplus" %% "scalatestplus-junit" % "1.0.0-M2"



