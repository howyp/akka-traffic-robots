package io.github.howyp

import io.github.howyp.test.parsers.ParseResultMatchers
import org.scalatest.{FreeSpec, Matchers}

class RouteWaypointSpec extends FreeSpec with Matchers with ParseResultMatchers {
  "Route Waypoints" - {
    "can be parsed from a CSV line" - {
      "given a valid example then it should extract robot ID, timestamp and location" in {
        RouteWaypoint.parseLine("5937,\"51.476002\",\"-0.096826\",\"2011-03-22 07:58:05\"") should parseTo (
          RouteWaypoint(timestamp = "2011-03-22 07:58:05", location = Location(51.476002, -0.096826))
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
  }
}