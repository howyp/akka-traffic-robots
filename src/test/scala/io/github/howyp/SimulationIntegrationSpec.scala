package io.github.howyp

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import io.github.howyp.test.actors.EventStreamListening
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

class SimulationIntegrationSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with FreeSpecLike
  with Matchers
  with BeforeAndAfterAll
  with EventStreamListening {

  def this() = this(ActorSystem("SimulationIntegrationSpec"))

  listenOnEventStreamFor(classOf[SimulationEvent])

  "The simulation" - {
    "should emit two traffic reports when each robot is sent to waypoints near stations" in {
      val locationZero = Location(0,0)
      val locationNearZero = Location(0.001,0.001)

      locationZero.distanceInMeters(locationNearZero) should be < 350.0

      val firstRobotId = 1
      val secondRobotId = 2
      val time1 = LocalDateTime.now()
      val s = new Simulation {
        val system = _system
        val waypointSource = Map(
          firstRobotId -> Stream(RouteWaypoint(timestamp = time1, location = locationZero)),
          secondRobotId -> Stream(RouteWaypoint(timestamp = time1, location = locationNearZero))
        )
        val tubeStations = List(TubeStation("Mornington Cresent", locationZero))
        val trafficConditionGenerator = () => TrafficCondition.Light
      }

      s.run()

      eventStream.receiveN(4) should contain allOf (
        TrafficReport(
          robotId = firstRobotId,
          timestamp = RouteWaypoint(timestamp = time1, location = locationZero).timestamp,
          speed = 0,
          condition = TrafficCondition.Light
        ),
        TrafficReport(
          robotId = secondRobotId,
          timestamp = RouteWaypoint(timestamp = time1, location = locationNearZero).timestamp,
          speed = 0,
          condition = TrafficCondition.Light
        )
      )
    }

  }

  override def afterAll {
    system.shutdown()
  }
}

