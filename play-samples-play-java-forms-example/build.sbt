name := """play-java-forms-example"""

version := "2.8.x"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.1"

testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

libraryDependencies += guice

// https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.2"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.7"

// disabled until https://github.com/playframework/playframework/issues/9845 is solved
//scalacOptions ++= List("-encoding", "utf8", "-Xfatal-warnings", "-deprecation")
javacOptions ++= Seq(
  "-Xlint:unchecked",
  "-Xlint:deprecation",
  "-Werror"
) 
