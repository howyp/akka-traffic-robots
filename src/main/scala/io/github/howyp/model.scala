package io.github.howyp

import scala.util.parsing.combinator.JavaTokenParsers

case class Location(latitude: Double, longitude: Double)
case class TubeStation(name: String, location: Location)

object TubeStation extends JavaTokenParsers {
  override protected val whiteSpace = """[ \t]""".r
  def quotedString: TubeStation.Parser[String] = '"' ~> "[^\"]*".r <~ '"'
  def double: Parser[Double] = floatingPointNumber map (_.toDouble)

  def station: Parser[TubeStation] = (quotedString ~ "," ~ double ~ "," ~ double) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
  def stations: Parser[List[TubeStation]] = repsep(station, "\n")
}