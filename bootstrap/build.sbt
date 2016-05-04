name := "multichain-experiment-1"

organization := "uk.gov.dwp"

version := "0.0.1"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

resolvers += "dhpcs at bintray" at "https://dl.bintray.com/dhpcs/maven"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.dhpcs" %% "play-json-rpc" % "1.0.0")
