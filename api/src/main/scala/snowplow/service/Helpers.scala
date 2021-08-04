package snowplow.service

import cats.effect.Sync
import cats.{Applicative, Monad}
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{EntityDecoder, HttpRoutes, Response, Uri}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import snowplow.models.HttpResponces._
import play.api.libs.json.Json
import snowplow.algebras.JsonSchemaAlg

object Helpers {

  //Recursivly clean JSValues of nulls
  def withoutNull(json: JsValue): JsValue =
    json match {
      case JsObject(f) =>
        JsObject(f.flatMap {
          case (_, JsNull) => None
          case n @ (name, nestedValue) =>
            Some(name -> withoutNull(nestedValue))
        })
      case other => other
    }

  def validateJson(requestBody: String): Option[JsValue] = {
    try {
      Some(Json.parse(requestBody))
    } catch {
      case e: JsonParseException => None
    }
  }

  def validateWithSchema[F[_]: Applicative](
      document: String,
      schema: String
  ): F[Either[String, Unit]] = {
    Applicative[F].pure(try {
      val validateSchema: ProcessingReport =
        JsonSchemaFactory.byDefault
          .getJsonSchema(buildJsNode(schema))
          .validate(buildJsNode(document))

      if (validateSchema.isSuccess) Right(())
      else {
        val it = validateSchema.iterator()
        if (it.hasNext) {
          Left(it.next().getMessage)
        } else {
          Left("Unknown error")
        }
      }
    } catch {
      case e: JsonParseException => Left(e.getMessage)
    })
  }

  private def buildJsNode(schema: String) = {
    JsonLoader.fromString(schema.toString())
  }
}
