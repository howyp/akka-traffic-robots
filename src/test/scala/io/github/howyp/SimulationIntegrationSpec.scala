package io.github.howyp

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FreeSpecLike, BeforeAndAfterAll, Matchers, FreeSpec}

class SimulationIntegrationSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with FreeSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("SimulationIntegrationSpec"))


  "The simulation" - {
    "should emit one traffic report when there is one waypoint" in {
      val s = new Simulation {
        val system = _system
        val waypointSource = Stream(RouteWaypoint(1, "1", Location(1.0, 1.0)))
        val trafficConditionGenerator = () => TrafficCondition.Light
      }
      system.eventStream.subscribe(self, classOf[TrafficReport])

      s.run()

      expectMsg(TrafficReport(1, "1", 30, TrafficCondition.Light))
    }
  }

  override def afterAll {
    system.shutdown()
  }
}
