package io.github.howyp

import io.github.howyp.parse.CsvParser

import scala.util.parsing.combinator.JavaTokenParsers

case class Location(latitude: Double, longitude: Double)

case class TubeStation(name: String, location: Location)
object TubeStation extends CsvParser[TubeStation] {
  def record: Parser[TubeStation] = (quotedString ~ "," ~ double ~ "," ~ double) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
}

case class RouteWaypoint(robotId: Int, timestamp: String, location: Location)
object RouteWaypoint extends CsvParser[RouteWaypoint] {
  def record: Parser[RouteWaypoint] = (integer ~ "," ~ quotedDouble ~ "," ~ quotedDouble ~ "," ~ quotedString) map {
    case robotId ~ _ ~ latitude ~ _ ~ longitude ~ _ ~ timestamp => RouteWaypoint(robotId, timestamp, Location(latitude, longitude))
  }
}