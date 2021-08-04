package snowplow.database

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.syntax.functor._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.SelfAwareStructuredLogger
import snowplow.algebras.JsonSchemaAlg
import snowplow.models.DatabaseModels.JsonSchema

class JsonSchemaReal[F[_]: Sync](
    transactor: Transactor[F],
    logger: SelfAwareStructuredLogger[F]
) extends JsonSchemaAlg[F] {

  override def createJsonSchema(jsonSchema: JsonSchema): F[Int] = {
    JsonSchemaReal.createJsonSchemaQuery
      .updateMany(NonEmptyList.one(jsonSchema))
      .transact(transactor)
  }

  override def getJsonSchemaQuery(id: String): F[Option[JsonSchema]] = {
    JsonSchemaReal.getJsonSchemaQuery(id).to[List].transact(transactor).fmap(_.headOption)
  }

}

object JsonSchemaReal {

  def createJsonSchemaQuery: Update[JsonSchema] =
    Update[JsonSchema]("""
    INSERT INTO json_schema (id, jsonSchema) VALUES (?, ?)""")

  def getJsonSchemaQuery(id: String): Query0[JsonSchema] = {
    sql"""SELECT * FROM json_schema WHERE id = $id""".query[JsonSchema]
  }

}
