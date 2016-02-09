package io.github.howyp

import java.time.LocalDateTime

import io.github.howyp.parse.CsvParser

import scala.util.Random

//TODO getting a bit big?

case class Location(latitude: Double, longitude: Double) {

  def distanceInMeters(other: Location): Double = {
    val deltaLat = math.toRadians(other.latitude - this.latitude)
    val deltaLong = math.toRadians(other.longitude - this.longitude)
    val a = math.pow(math.sin(deltaLat / 2), 2) + math.cos(math.toRadians(this.latitude)) * math.cos(math.toRadians(other.latitude)) * math.pow(math.sin(deltaLong / 2), 2)
    val greatCircleDistance = 2 * math.asin(math.sqrt(a))
    Location.radiusOfEarthKm * greatCircleDistance * 1000
  }
}
object Location {
  val radiusOfEarthKm = 6372.8
}

case class TubeStation(name: String, location: Location)
object TubeStation extends CsvParser[TubeStation] {
  def record: Parser[TubeStation] = (quotedString ~ "," ~ double ~ "," ~ double) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
}

case class RouteWaypoint(timestamp: LocalDateTime, location: Location)
object RouteWaypoint extends CsvParser[RouteWaypoint] {
  def record: Parser[RouteWaypoint] = (integer ~ "," ~ quotedDouble ~ "," ~ quotedDouble ~ "," ~ quotedTimestamp) map {
    case robotId ~ _ ~ latitude ~ _ ~ longitude ~ _ ~ timestamp =>
      RouteWaypoint(timestamp, Location(latitude, longitude))
  }
}

trait TrafficCondition
object TrafficCondition {
  def random(): TrafficCondition = Random.nextInt(3) match {
    case 0 => Light
    case 1 => Medium
    case 2 => Heavy
  }

  case object Light extends TrafficCondition
  case object Medium extends TrafficCondition
  case object Heavy extends TrafficCondition
}