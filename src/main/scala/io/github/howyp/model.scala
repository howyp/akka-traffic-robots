package io.github.howyp

import java.security.Timestamp

import io.github.howyp.parse.CsvParser

import scala.util.Random
import scala.util.parsing.combinator.JavaTokenParsers

//TODO getting a bit big?

case class Location(latitude: Double, longitude: Double)

case class TubeStation(name: String, location: Location)
object TubeStation extends CsvParser[TubeStation] {
  def record: Parser[TubeStation] = (quotedString ~ "," ~ double ~ "," ~ double) map {
    case name ~ _ ~ latitude ~ _ ~ longitude => TubeStation(name, Location(latitude, longitude))
  }
}

case class RouteWaypoint(timestamp: String, location: Location)
object RouteWaypoint extends CsvParser[RouteWaypoint] {
  def record: Parser[RouteWaypoint] = (integer ~ "," ~ quotedDouble ~ "," ~ quotedDouble ~ "," ~ quotedString) map {
    case robotId ~ _ ~ latitude ~ _ ~ longitude ~ _ ~ timestamp => RouteWaypoint(timestamp, Location(latitude, longitude))
  }
}

trait SimulationEvent
case class TrafficReport(robotId: RobotId, timestamp: String, speed: Int, condition: TrafficCondition) extends SimulationEvent
case class RobotMoved(newLocation: Location) extends SimulationEvent

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