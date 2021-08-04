package models

import play.api.libs.json.{Format, Json, JsonConfiguration, OptionHandlers}

object HttpResponces {

  //Removes Null values
  implicit val config = JsonConfiguration(optionHandlers = OptionHandlers.Default)

  case class SchemaResult(
      action: String,
      id: String,
      status: String,
      message: Option[String] = None
  )

  object SchemaResult {
    implicit val apiConfigFormat: Format[SchemaResult] = Json.format[SchemaResult]
  }
}
