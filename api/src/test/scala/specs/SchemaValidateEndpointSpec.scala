package specs

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import snowplow.models.DatabaseModels.JsonSchema
import snowplow.models.HttpResponces.SchemaResult
import org.http4s
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Status, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import play.api.libs.json.Json
import Helpers.{buildUri, getConfig}
import snowplow.algebras.JsonSchemaAlg
import snowplow.service.SchemaValidateEndpoint

import scala.language.reflectiveCalls

class SchemaValidateEndpointSpec extends AnyFreeSpec with MockFactory with Matchers {

  def fixture() =
    new {
      val dbAccess                              = stub[JsonSchemaAlg[IO]]
      val logger: SelfAwareStructuredLogger[IO] = stub[SelfAwareStructuredLogger[IO]]
      val endpoint: HttpRoutes[IO]              = new SchemaValidateEndpoint[IO](dbAccess, logger).routes

      val data_1: JsonSchema =
        JsonSchema(
          "schema_1",
          getConfig("config-schema.json")
        )

      val document: String            = getConfig("config.json")
      val documentNotMatching: String = getConfig("config-no-match.json")

      //Mock result
      (dbAccess.createJsonSchema _).when(data_1).returns(IO.pure(1))
      (dbAccess.getJsonSchemaQuery _).when(data_1.id).returns(IO.pure(Some(data_1)))

      //Missing data from DB
      (dbAccess.getJsonSchemaQuery _).when("Invalid_Schema_ID").returns(IO.pure(None))

    }

  private def generateRequest(request: Request[IO], route: HttpRoutes[IO]): http4s.Response[IO] =
    route.orNotFound(request).unsafeRunSync()

  "Calling on /Schema/Id " - {
    "with a POST request " - {

      "with an invalid document schema should return a bad request status code" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_1.id}"))
            .withEntity("This is an example of invalid Json.")

        generateRequest(request, f.endpoint).status shouldBe Status.BadRequest
      }
      "with a valid document schema should return a 200 status code" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_1.id}"))
            .withEntity(f.document)

        generateRequest(request, f.endpoint).status shouldBe Status.Ok
      }

      "with a valid Json document but a missing schema should should return a Not found status code" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri("Invalid_Schema_ID")).withEntity(f.document)
        generateRequest(request, f.endpoint).status shouldBe Status.NotFound
      }

      "with a valid Json document that is invalid against the stored schema, should return a Json error" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_1.id}"))
            .withEntity(f.documentNotMatching)

//        generateRequest(request, f.endpoint).status shouldBe Status.BadRequest

        generateRequest(request, f.endpoint).as[String].unsafeRunSync() shouldBe
          Json
            .toJson(
              SchemaResult(
                "validateDocument",
                "schema_1",
                "error",
                Some(
                  """Message: instance type (string) does not match any allowed primitive type (allowed: ["integer"])"""
                )
              )
            )
            .toString()
      }

    }

  }

}
