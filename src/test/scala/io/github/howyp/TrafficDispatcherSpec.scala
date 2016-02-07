package io.github.howyp

import akka.testkit.TestFSMRef
import io.github.howyp.test.actors.ActorSpec
import org.scalatest.{FreeSpec, Matchers}

class TrafficDispatcherSpec extends FreeSpec with Matchers with ActorSpec {
  import TrafficDispatcher._

  "The traffic dispatcher" - {
    val dispatcher = TestFSMRef(new TrafficDispatcher(() => TrafficCondition.Light))
    "start in an initial state" in {
      dispatcher should have ('stateName (State.Initialised), 'stateData (Data.Empty))
    }
    "allow a set of waypoints to be configured" in {
      val points = List(
        RouteWaypoint(robotId = 5937, location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:40"),
        RouteWaypoint(robotId = 5937, location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:41"),
        RouteWaypoint(robotId = 5937, location = Location(51.487492,-0.095382), timestamp = "2011-03-22 08:01:42"),
        RouteWaypoint(robotId = 5937, location = Location(51.487545,-0.095404), timestamp = "2011-03-22 08:01:43")
      )
      dispatcher ! Protocol.AddWaypointSource(points.toStream)
      dispatcher.stateData.asInstanceOf[Data.Waypoints].w.toList should contain theSameElementsInOrderAs (points)
    }
    "set up robots with the given IDs to patrol the town" in pending
    "should reply with a set of routes when requested" in {

    }
  }
}

