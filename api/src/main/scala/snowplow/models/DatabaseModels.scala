package snowplow.models

import play.api.libs.json.{Format, Json}

object DatabaseModels {
  //Unique key for json schema
  case class JsonSchema(id: String, jsonschema: String)

  object JsonSchema {
    implicit val apiConfigFormat: Format[JsonSchema] = Json.format[JsonSchema]
  }
}
