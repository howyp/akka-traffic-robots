package io.github.howyp

import akka.testkit.{TestActorRef, TestProbe}
import io.github.howyp.test.actors.{ActorSpec, EventStreamListening}
import org.scalatest.{FreeSpec, Matchers}

class RobotSpec extends FreeSpec with Matchers with ActorSpec with EventStreamListening {
  "A Robot" - {
    val locationWithoutATubeStation = Location(1.0, 1.0)
    val tubeStation = TubeStation("a", Location(2.0, 2.0))
    val id: RobotId = 1234

    val dispatcher = TestProbe()
    val robot = TestActorRef(
      props = Robot.props(id, List(tubeStation), () => TrafficCondition.Heavy),
      supervisor = dispatcher.ref,
      name = "robot"
    )
    listenOnEventStreamFor(classOf[SimulationEvent])

    "should request some waypoints from the dispatcher on startup" in {
      dispatcher.expectMsg(TrafficDispatcher.Protocol.MorePointsRequired(id))
    }

    "after receiving a waypoint, should travel to that point" in {
      robot ! TrafficDispatcher.Protocol.VisitWaypoint(RouteWaypoint(timestamp = "1", location = locationWithoutATubeStation))
      eventStream.expectMsg(RobotMoved(locationWithoutATubeStation))
    }

    "after receiving a waypoint that is in the same location as the tube station, emit" +
      "a traffic report for that location" in {
      robot ! TrafficDispatcher.Protocol.VisitWaypoint(RouteWaypoint(timestamp = "2", tubeStation.location))
      eventStream.expectMsgAllOf(
        RobotMoved(tubeStation.location),
        TrafficReport(
          robotId = id,
          timestamp = "2",
          speed = 0,
          condition = TrafficCondition.Heavy
        )
      )
    }

    "after processing all of the configured waypoint batch, request more" in {
      robot ! TrafficDispatcher.Protocol.EndOfWaypointBatch
      dispatcher.expectMsg(TrafficDispatcher.Protocol.MorePointsRequired(id))
    }
  }
}
