package snowplow.models

object ConfigModels {
  case class ServerConfig(host: String, port: Int)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  case class DatabaseConfig(
      driver: String,
      url: String,
      user: String,
      password: String
  )
}
