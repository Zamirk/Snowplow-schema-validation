package service

import snowplow.JsonSchemaReal
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import cats.effect.kernel.Async
import cats.effect.Resource
import models.ConfigModels.Config
import org.http4s.HttpRoutes
import org.http4s.Status.Ok
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.{Router, Server}
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{EntityDecoder, HttpRoutes, MediaType, Uri}
import snowplow.JsonSchemaReal
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import play.api.libs.json.Json._
import models.HttpResponces._
import models.DatabaseModels._
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.global

object HTTPServer {

  def createBlazeServer[F[_]: Async](
      config: Config,
      logger: SelfAwareStructuredLogger[F],
      repository: JsonSchemaReal[F]
  ): Resource[F, Server] = {
    BlazeServerBuilder[F](global)
      .bindHttp(config.server.port, config.server.host)
      .withHttpApp(
        Router(
          "/schema"   -> SchemaProcessingEndpoint[F](repository, logger).routes,
          "/validate" -> SchemaValidateEndpoint[F](repository, logger).routes
        ).orNotFound
      )
      .resource
  }

}
