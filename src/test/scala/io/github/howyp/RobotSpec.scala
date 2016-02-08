package io.github.howyp

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import io.github.howyp.test.actors.{EventStreamListening, ActorSpec}
import org.scalatest.{Matchers, FreeSpec}

class RobotSpec extends FreeSpec with Matchers with ActorSpec with EventStreamListening {
  "A Robot" - {
    val dispatcher = TestProbe()
    val robot = TestActorRef(props = Props(new Robot(List.empty)), supervisor = dispatcher.ref, name = "robot")
    listenOnEventStreamFor(classOf[SimulationEvent])

    "should request some waypoints from the dispatcher on startup" in {
      dispatcher.expectMsg(TrafficDispatcher.Protocol.MorePointsRequired)
    }

    "after receiving a waypoint, should travel to that point" in {
      val location = Location(1.0, 1.0)
      robot ! TrafficDispatcher.Protocol.VisitWaypoint(RouteWaypoint(timestamp = "1", location = location))
      eventStream.expectMsg(RobotMoved(location))
    }
  }
}
