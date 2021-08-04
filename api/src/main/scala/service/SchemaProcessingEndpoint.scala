package service

import algebras.JsonSchemaAlg
import cats.effect._
import cats.implicits._
import com.fasterxml.jackson.core.JsonParseException
import fs2.Stream
import org.http4s.dsl.Http4sDsl
import snowplow.JsonSchemaReal
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{EntityDecoder, HttpRoutes, MediaType, Response, Uri}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import models.HttpResponces._
import models.DatabaseModels._
import play.api.libs.json.{JsObject, JsValue, Json}
import Helpers._

case class SchemaProcessingEndpoint[F[_]: Sync](
    repository: JsonSchemaAlg[F],
    logger: SelfAwareStructuredLogger[F]
)(implicit
    F: Async[F]
) extends Http4sDsl[F] {
  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> path =>
        for {
          requestBody <- EntityDecoder.decodeText(req)
          result <- validateJson(requestBody) match {
            case None => {
              println("ay " + requestBody)
              println("ay " + requestBody)
              println("ay " + requestBody)
              println("ay " + requestBody)
              println("ay " + requestBody)
              println("ay " + requestBody)
              println("ay " + requestBody)

              badRequest(path)
            }
            case Some(x) => processJson(path, x)
          }
        } yield result
      case req @ GET -> path =>
        for {
          schema <- repository.getJsonSchemaQuery(path.toString())
          result <- schema.map(a => Json.toJson(a)) match {
            case Some(v) => Ok(v.toString())
            case None    => NotFound()
          }
        } yield result
    }

  private def processJson(path: Path, x: JsValue) = {
    for {

      _ <- repository.createJsonSchema(JsonSchema(path.toString(), x.toString()))
      response <- Created(
        Json
          .toJson(SchemaResult("uploadSchema", path.toString(), "success"))
          .toString(),
        Location(Uri.unsafeFromString(s"/schema/$path"))
      )
    } yield response
  }

  private def badRequest(path: Path): F[Response[F]] = {
    BadRequest(
      Json
        .toJson(
          SchemaResult("uploadSchema", path.toString(), "error", Some("Invalid JSON"))
        )
        .toString()
    )
  }

}
