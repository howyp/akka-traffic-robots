package io.github.howyp

import java.time.LocalDateTime

import io.github.howyp.parse.CsvParser

import scala.util.Random

case class Location(latitude: Double, longitude: Double) {

  def distanceInMeters(other: Location) = {
    Haversine.haversine(this.latitude, this.longitude, other.latitude, other.longitude) * 1000
  }
}

case class TubeStation(name: String, location: Location)
object TubeStation extends CsvParser[TubeStation] {
  def record: Parser[TubeStation] = (quoted(string) ~ "," ~ double ~ "," ~ double) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
}

case class RouteWaypoint(timestamp: LocalDateTime, location: Location)
object RouteWaypoint extends CsvParser[RouteWaypoint] {
  def record: Parser[RouteWaypoint] = (integer ~ "," ~ quoted(double) ~ "," ~ quoted(double) ~ "," ~ quoted(timestamp)) map {
    case robotId ~ _ ~ latitude ~ _ ~ longitude ~ _ ~ timestamp =>
      RouteWaypoint(timestamp, Location(latitude, longitude))
  }
}