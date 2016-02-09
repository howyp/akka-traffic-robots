package io.github.howyp

import java.time.LocalDateTime

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import io.github.howyp.test.actors.EventStreamListening
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike, Matchers}

class SimulationIntegrationSpec(_system: ActorSystem) extends TestKit(_system)
  with ImplicitSender
  with FreeSpecLike
  with Matchers
  with BeforeAndAfterAll
  with EventStreamListening
  with Eventually {

  def this() = this(ActorSystem("SimulationIntegrationSpec"))

  listenOnEventStreamFor(classOf[SimulationEvent])

  "The simulation" - {
    "in a very simple situation should integrate together to emit traffic reports and then shut down" in new TestData {
      val s = new Simulation {
        val system = _system
        val waypointSource = Map(
          firstRobotId -> Stream(RouteWaypoint(timestamp = time1, location = tubeStation.location)),
          secondRobotId -> Stream(RouteWaypoint(timestamp = time1, location = locationNearTubeStation))
        )
        val tubeStations = List(tubeStation)
        val trafficConditionGenerator = () => TrafficCondition.Light
      }

      s.run()

      eventStream.receiveN(4) should contain allOf (
        TrafficReport(
          robotId = firstRobotId,
          timestamp = RouteWaypoint(timestamp = time1, location = tubeStation.location).timestamp,
          speed = 0,
          condition = TrafficCondition.Light
        ),
        TrafficReport(
          robotId = secondRobotId,
          timestamp = RouteWaypoint(timestamp = time1, location = locationNearTubeStation).timestamp,
          speed = 0,
          condition = TrafficCondition.Light
        )
      )

      eventually (system should be ('terminated))
    }
  }
  trait TestData {
    val tubeStation = TubeStation("Mornington Cresent", Location(0,0))
    val locationNearTubeStation = Location(0.001,0.001)
    val firstRobotId = 1
    val secondRobotId = 2
    val time1 = LocalDateTime.parse("2011-03-22T08:01:42")
  }
}
