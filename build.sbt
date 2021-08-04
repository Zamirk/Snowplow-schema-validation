ThisBuild / version := "0.1"

ThisBuild / scalaVersion := "2.13.5"

ThisBuild / scalacOptions ++= Seq(
//  "-Xfatal-warnings",
  "-feature",
  "-deprecation",
  "-language:higherKinds",
  "-unchecked"
)

import Dependencies._

ThisBuild / publish / skip := true

lazy val root = (project in file("."))
  .settings(
    name := "snowplow-schema-validation"
  )
  .aggregate(api)

lazy val api = (project in file("api"))
  .settings(
    libraryDependencies ++= allLibs.toList
  )
