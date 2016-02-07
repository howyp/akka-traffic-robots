package io.github.howyp

import io.github.howyp.test.parsers.ParseResultMatchers
import org.scalatest.{FreeSpec, Matchers}

import scala.collection.immutable.List

class RouteWaypointSpec extends FreeSpec with Matchers with ParseResultMatchers {
  "Route Waypoints" - {
    "can be parsed from a CSV line" - {
      "given a valid example then it should extract robot ID, timestamp and location" in {
        RouteWaypoint.parseLine("5937,\"51.476002\",\"-0.096826\",\"2011-03-22 07:58:05\"") should parseTo (
          RouteWaypoint(robotId = 5937, timestamp = "2011-03-22 07:58:05", location = Location(51.476002, -0.096826))
        )
      }
      "given a line with a missing quote then it should not parse" in {
        RouteWaypoint.parseLine("5937,\"51.476002,\"-0.096826\",\"2011-03-22 07:58:05\"") should failParsing
      }
      "given a line with a missing field then it should not parse" in {
        RouteWaypoint.parseLine("5937,\"51.476002\",\"2011-03-22 07:58:05\"") should failParsing
      }
      "given a line with an incorrectly typed field then it should not parse" in {
        RouteWaypoint.parseLine("5937,\"51.476002\",\"-0.096826\",2011-03-22") should failParsing
      }
    }
    "should be loadable as a set from a file" - {
      "given a sample CSV string then it should return a list of route waypoints" in {
        val csvLines =
          """5937,"51.487381","-0.095346","2011-03-22 08:01:40"
            |5937,"51.487434","-0.095362","2011-03-22 08:01:41"
            |5937,"51.487492","-0.095382","2011-03-22 08:01:42"
            |5937,"51.487545","-0.095404","2011-03-22 08:01:43"""".stripMargin
        RouteWaypoint.parseLines(csvLines) should parseTo (
          List(
            RouteWaypoint(robotId = 5937, location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:40"),
            RouteWaypoint(robotId = 5937, location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:41"),
            RouteWaypoint(robotId = 5937, location = Location(51.487492,-0.095382), timestamp = "2011-03-22 08:01:42"),
            RouteWaypoint(robotId = 5937, location = Location(51.487545,-0.095404), timestamp = "2011-03-22 08:01:43")
          )
        )
      }
    }
  }
}