package snowplow.service

import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{EntityDecoder, HttpRoutes, Response, Uri}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import snowplow.models.HttpResponces._
import play.api.libs.json.Json
import Helpers.{validateJson, withoutNull}
import snowplow.algebras.JsonSchemaAlg

case class SchemaValidateEndpoint[F[_]: Sync](
    db: JsonSchemaAlg[F],
    logger: SelfAwareStructuredLogger[F]
)(implicit
    F: Async[F]
) extends Http4sDsl[F] {
  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> path =>
        for {
          document <- EntityDecoder.decodeText(req)
          schema   <- db.getJsonSchemaQuery(path.toString())
          result <- schema.map(_.jsonschema) match {
            case None => NotFound("The schema was not found.")
            case Some(jsonSchema) => {
              validateJson(document).map(withoutNull) match {
                case None => BadRequest("The document is invalid Json.")
                case Some(validDocument) =>
                  Helpers.validateWithSchema[F](validDocument.toString(), jsonSchema).flatMap {
                    case Left(error) => documentNotValidAgainstSchema(path, error)
                    case Right(_)    => successfulResult(path)
                  }
              }

            }
          }
        } yield result
    }

  private def successfulResult(path: Path): F[Response[F]] = {
    Ok(
      Json.toJson(SchemaResult("validateDocument", path.toString(), "success")).toString(),
      Location(Uri.unsafeFromString(s"/schema/$path"))
    )
  }

  private def documentNotValidAgainstSchema(path: Path, errors: String): F[Response[F]] = {
    BadRequest(
      Json
        .toJson(
          SchemaResult("validateDocument", path.toString(), "error", Some("Message: " + errors))
        )
        .toString()
    )
  }
}
