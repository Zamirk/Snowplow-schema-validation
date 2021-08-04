package snowplow

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import doobie.ExecutionContexts
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server
import org.typelevel.log4cats.SelfAwareStructuredLogger
import pureconfig.ConfigSource
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

import scala.concurrent.ExecutionContext.global
import cats.effect.kernel.Async
import cats.effect.Resource
import doobie.hikari.HikariTransactor
import models.ConfigModels.Config
import org.flywaydb.core.Flyway
import service.{HTTPServer, SchemaProcessingEndpoint, SchemaValidateEndpoint}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.{Router, Server}
import snowplow.DatabaseConnection.createDatabaseTables

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      logger     <- Resource.liftK(Slf4jLogger.create[IO]) // Creates logging facility
      _          <- Resource.liftK(logger.info(s"Application start.")) //Our first log
      config     <- Resource.liftK(ConfigSource.default.loadF[IO, Config]()) //Load config
      transactor <- DatabaseConnection.dbConnection[IO](config, logger)
      _          <- createDatabaseTables(transactor)
      repository = new JsonSchemaReal[IO](transactor, logger)
      server <- HTTPServer.createBlazeServer[IO](config, logger, repository)
    } yield {
      server
    }).use(_ => IO.never).as(ExitCode.Success)
  }

}
