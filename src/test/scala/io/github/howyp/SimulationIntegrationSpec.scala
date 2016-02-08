package io.github.howyp

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
    "should emit one traffic report when there is one waypoint at the same location as a station" in {
      val locationZero = Location(0,0)
      val firstRobotId = 1
      val waypointAtTime1 = RouteWaypoint(timestamp = "1", location = locationZero)
      val s = new Simulation {
        val system = _system
        val waypointSource = Map(firstRobotId -> Stream(waypointAtTime1))
        val tubeStations = List(TubeStation("Mornington Cresent", locationZero))
        val trafficConditionGenerator = () => TrafficCondition.Light
      }

      s.run()

      eventStream.expectMsg(TrafficReport(robotId = 1, timestamp = "1", speed = 30, condition = TrafficCondition.Light))
//      eventStream.expectMsg(
//        TrafficReport(
//          robotId = firstRobotId,
//          timestamp = waypointAtTime1.timestamp,
//          speed = 0,
//          condition = TrafficCondition.Light
//        )
//      )
    }
  }

  override def afterAll {
    system.shutdown()
  }
}

