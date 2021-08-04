package snowplow

import pureconfig.module.catseffect.syntax.CatsEffectConfigSource
import snowplow.database.{DatabaseConnection, JsonSchemaReal}
import service.{HTTPServer, SchemaProcessingEndpoint, SchemaValidateEndpoint}
import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import pureconfig.ConfigSource
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._
import cats.effect.Resource
import database.DatabaseConnection
import database.DatabaseConnection.createDatabaseTables
import models.ConfigModels.Config
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
