package snowplow.database

import cats.effect.Resource
import cats.effect.kernel.Async
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.typelevel.log4cats.SelfAwareStructuredLogger
import doobie.ExecutionContexts
import org.typelevel.log4cats.SelfAwareStructuredLogger
import cats.effect.kernel.Async
import cats.effect.Resource
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import snowplow.models.ConfigModels.Config

object DatabaseConnection {

  def dbConnection[F[_]](
      loadedConfig: Config,
      logger: SelfAwareStructuredLogger[F]
  )(implicit s: Async[F]): Resource[F, HikariTransactor[F]] = {
    for {
      _  <- Resource.liftK(logger.info("Creating db connection."))
      ec <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
      db <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver", // driver classname
        loadedConfig.database.url,
        loadedConfig.database.user,     // username
        loadedConfig.database.password, // password
        ec                              // await connection here
      )
    } yield db
  }

  //Creates the table in db.migration if it does not exist
  def createDatabaseTables[F[_]: Async](transactor: HikariTransactor[F]): Resource[F, Unit] = {
    Resource.liftK(
      transactor.configure { dataSource =>
        Async[F].pure {
          Flyway.configure().dataSource(dataSource).load().migrate()
          ()
        }
      }
    )
  }
}
