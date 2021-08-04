import sbt.{ModuleID, _}

object Dependencies {

  object Version {
    lazy val classicLogger = "1.0.13" //For logging

    lazy val doobie        = "1.0.0-M2"  //Database interaction
    lazy val Http4sVersion = "1.0.0-M23" //Need this version for CE 3.0

    val ce         = "3.1.0"
    val cats       = "2.6.0"
    val pureconfig = "0.15.0" //Used for reading config

    val scalaTest     = "3.1.1"
    val scalatestplus = "3.1.1.1"
    val fs2           = "2.5.9"
    val log4Cats      = "2.1.0"

    val Specs2Version         = "4.10.5"
    val pureconfigVersion     = "0.14.0"
    val fs2Data               = "0.9.0"
    val doobieVersion         = "0.9.0"
    val log4cats              = "1.1.1"
    lazy val H2Version        = "1.4.200"
    lazy val FlywayVersion    = "7.5.2"
    lazy val CirceVersion     = "0.13.0"
    lazy val LogbackVersion   = "1.2.3"
    lazy val ScalaTestVersion = "3.2.3"
    lazy val ScalaMockVersion = "5.1.0"
  }

  lazy val log4cats: Seq[ModuleID] = List(
    "org.typelevel" %% "log4cats-core"  % Version.log4Cats,
    "org.typelevel" %% "log4cats-slf4j" % Version.log4Cats,
    // Classic logger is required for log4cats to work
    "ch.qos.logback" % "logback-classic" % Version.classicLogger
  )

  lazy val cats: Seq[ModuleID] = List(
    "org.typelevel" %% "cats-core" % Version.cats
  )

  lazy val flyway: Seq[ModuleID] = List(
    "org.flywaydb" % "flyway-core" % Version.FlywayVersion
  )

  lazy val play: Seq[ModuleID] = List(
    "de.heikoseeberger" %% "akka-http-play-json" % "1.35.2",
    "com.typesafe.play" %% "play-json"           % "2.8.1"
  )

  lazy val http4s: Seq[ModuleID] = List(
    "org.http4s" %% "http4s-blaze-server" % Version.Http4sVersion, //Used for creating the server
    "org.http4s" %% "http4s-dsl"          % Version.Http4sVersion
  )

  lazy val doobie: Seq[ModuleID] = List(
    "org.tpolecat" %% "doobie-core"     % Version.doobie,
    "org.tpolecat" %% "doobie-h2"       % Version.doobie, // H2 driver 1.4.200 + type mappings.
    "org.tpolecat" %% "doobie-hikari"   % Version.doobie, // HikariCP transactor.
    "org.tpolecat" %% "doobie-postgres" % Version.doobie // Postgres driver 42.2.19 + type mappings
  )

  lazy val validator: Seq[ModuleID] = List(
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
  )

  lazy val testLibs: Seq[ModuleID] = List(
    "org.scalatest" %% "scalatest" % Version.ScalaTestVersion % "it,test",
    "org.scalamock" %% "scalamock" % Version.ScalaMockVersion % "test"
  )

  lazy val pureConfig: Seq[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig"             % Version.pureconfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % Version.pureconfig
  )

  val allLibs: Seq[ModuleID] =
    pureConfig ++ http4s ++ cats ++ log4cats ++ doobie ++ flyway ++ play ++ testLibs ++ validator
}
