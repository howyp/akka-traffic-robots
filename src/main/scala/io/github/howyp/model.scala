package io.github.howyp

import scala.util.parsing.combinator.JavaTokenParsers

case class Location(latitude: Double, longitude: Double)
case class TubeStation(name: String, location: Location)

object TubeStation extends JavaTokenParsers {
  def name: Parser[String] = """[^"]*""".r
  def lat: Parser[Double] = floatingPointNumber map (_.toDouble)

  def csvField[T](fieldParser: Parser[T]): Parser[T] = ('"' ~> fieldParser <~ '"') | fieldParser

  def station: Parser[TubeStation] = csvField(name) ~ "," ~ csvField(lat) ~ "," ~ csvField(lat) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
}