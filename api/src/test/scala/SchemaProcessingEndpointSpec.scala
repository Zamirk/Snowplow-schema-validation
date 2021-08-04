package test
import Helpers.buildUri
import algebras.JsonSchemaAlg
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import models.DatabaseModels.JsonSchema
import org.http4s
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Status, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import play.api.libs.json.Json
import service.SchemaProcessingEndpoint

import scala.language.reflectiveCalls

class SchemaProcessingEndpointSpec extends AnyFreeSpec with MockFactory with Matchers {
  def fixture() =
    new {
      val dbAccess = stub[JsonSchemaAlg[IO]]
      val logger   = stub[SelfAwareStructuredLogger[IO]]
      val endpoint = new SchemaProcessingEndpoint[IO](dbAccess, logger).routes

      val data_1 =
        JsonSchema(
          "schema_1",
          Helpers.getConfig("config-schema.json")
        )

      val data_2 =
        JsonSchema(
          "schema_2",
          Helpers.getConfig("config-schema-invalid.json")
        )

      //Mock result
      (dbAccess.createJsonSchema _).when(data_1).returns(IO.pure(1))
      (dbAccess.getJsonSchemaQuery _).when(data_1.id).returns(IO.pure(Some(data_1)))

      //Missing data from DB
      (dbAccess.getJsonSchemaQuery _).when("invalid_id").returns(IO.pure(None))
    }

  private def generateRequest(request: Request[IO], route: HttpRoutes[IO]): http4s.Response[IO] =
    route.orNotFound(request).unsafeRunSync()

  "Calling on /Schema/Id " - {
    "with a POST request " - {
      "with a valid shema should return a 201 status" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_1.id}"))
            .withEntity(f.data_1.jsonschema)

        generateRequest(request, f.endpoint).status shouldBe Status.Created
      }

      "with a valid schema should return json response" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_1.id}"))
            .withEntity(f.data_1.jsonschema)

        generateRequest(request, f.endpoint).as[String].unsafeRunSync() shouldBe
          Json
            .toJson(
              models.HttpResponces.SchemaResult("uploadSchema", f.data_1.id, "success")
            )
            .toString()

      }

      "with an invalid schema should return not found status code" in {

        val f = fixture()

        val request =
          Request[IO](POST, buildUri(s"${f.data_2.id}"))
            .withEntity(f.data_2.jsonschema)

        generateRequest(request, f.endpoint).status shouldBe Status.BadRequest

      }
    }
    "with a GET request " - {
      "with a valid schema id should return an Ok status" in {

        val f = fixture()

        val request =
          Request[IO](GET, buildUri(s"${f.data_1.id}"))

        generateRequest(request, f.endpoint).status shouldBe Status.Ok
      }

      "with a valid schema id should return json response of the data" in {

        val f = fixture()

        val request =
          Request[IO](GET, buildUri(s"${f.data_1.id}"))

        generateRequest(request, f.endpoint)
          .as[String]
          .unsafeRunSync() shouldBe Json.toJson(f.data_1).toString()

      }

      "with an invalid schema id should return not found status code" in {

        val f = fixture()

        val request =
          Request[IO](GET, buildUri("invalid_id"))

        generateRequest(request, f.endpoint).status shouldBe Status.NotFound
      }
    }

  }

}
