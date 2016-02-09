package io.github.howyp

import java.time.LocalDateTime

import akka.testkit.{TestActorRef, TestProbe}
import io.github.howyp.test.actors.{ActorSpec, EventStreamListening}
import org.scalatest.{FreeSpec, Matchers}

class RobotSpec extends FreeSpec with Matchers with ActorSpec with EventStreamListening {
  "A Robot" - {
    val locationWithoutATubeStation = Location(1.0, 1.0)
    val tubeStation = TubeStation("a", Location(2.0, 2.0))
    val id: RobotId = 1234
    val timestamp1 = LocalDateTime.now()
    val timestamp2 = timestamp1.plusSeconds(1)
    val timestamp3 = timestamp2.plusSeconds(1)

    val dispatcher = TestProbe()
    val robot = TestActorRef(
      props = Robot.props(id, List(tubeStation), () => TrafficCondition.Heavy),
      supervisor = dispatcher.ref,
      name = "robot"
    )
    listenOnEventStreamFor(classOf[SimulationEvent])

    "should request some waypoints from the dispatcher on startup" in {
      dispatcher.expectMsg(Protocol.MorePointsRequired(id))
    }

    "after receiving a waypoint, should travel to that point" in {
      robot ! Protocol.VisitWaypoint(RouteWaypoint(timestamp = timestamp1, location = locationWithoutATubeStation))
      eventStream.expectMsg(RobotMoved(id, locationWithoutATubeStation))
    }

    "after receiving a waypoint that is in the same location as the tube station, emit a traffic report for that location" in {
      robot ! Protocol.VisitWaypoint(RouteWaypoint(timestamp = timestamp2, tubeStation.location))
      eventStream.expectMsgAllOf(
        RobotMoved(id, tubeStation.location),
        TrafficReport(
          robotId = id,
          timestamp = timestamp2,
          speed = 157269.8529731959,
          condition = TrafficCondition.Heavy
        )
      )
    }

    "after receiving a waypoint that is with 350m of a tube station, emit a traffic report for that location" in {
      val locationNearStation = Location(tubeStation.location.latitude, tubeStation.location.longitude + 0.001)
      robot ! Protocol.VisitWaypoint(RouteWaypoint(timestamp = timestamp3, locationNearStation))
      eventStream.expectMsgAllOf(
        RobotMoved(id, locationNearStation),
        TrafficReport(
          robotId = id,
          timestamp = timestamp3,
          speed = 111.15858648842158,
          condition = TrafficCondition.Heavy
        )
      )
    }

    "after processing all of the configured waypoint batch, request more" in {
      robot ! Protocol.EndOfWaypointBatch
      dispatcher.expectMsg(Protocol.MorePointsRequired(id))
    }

    "kill itself if asked to shut down" in {
      dispatcher.watch(robot)
      robot.!(Protocol.Shutdown)(dispatcher.ref)
      dispatcher.expectMsg(Protocol.ShutdownComplete(id))
      dispatcher.expectTerminated(robot)
    }
  }
}
