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
    "should emit one traffic report when there is one waypoint" in {
      val s = new Simulation {
        val system = _system
        val waypointSource = Map(1 -> Stream(RouteWaypoint(timestamp = "1", location = Location(1.0, 1.0))))
        val trafficConditionGenerator = () => TrafficCondition.Light
      }

      s.run()

      eventStream.expectMsg(TrafficReport(robotId = 1, timestamp = "1", speed = 30, condition = TrafficCondition.Light))
    }
  }

  override def afterAll {
    system.shutdown()
  }
}
