package snowplow.algebras

import cats.data.NonEmptyList
import snowplow.models.DatabaseModels.JsonSchema

trait JsonSchemaAlg[F[_]] {
  def createJsonSchema(jsonString: JsonSchema): F[Int]

  def getJsonSchemaQuery(id: String): F[Option[JsonSchema]]
}
