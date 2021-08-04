package specs

import org.http4s.Uri
import org.http4s.implicits.http4sLiteralsSyntax

import scala.io.Source

object Helpers {

  def buildUri(s: String) = Uri.fromString(s).getOrElse(uri"/")

  def getConfig(f: String): String =
    Source
      .fromResource(f)
      .getLines()
      .mkString("")
      .filterNot((x: Char) => x.isWhitespace)

}
