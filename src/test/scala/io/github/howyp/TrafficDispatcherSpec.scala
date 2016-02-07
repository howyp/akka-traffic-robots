package io.github.howyp

import akka.actor.{ActorRef, ActorRefFactory}
import akka.testkit.{TestProbe, TestActorRef, TestFSMRef}
import io.github.howyp.test.actors.ActorSpec
import org.scalatest.{FreeSpec, Matchers}

class TrafficDispatcherSpec extends FreeSpec with Matchers with ActorSpec {
  import TrafficDispatcher._

  "The traffic dispatcher" - {

    val robotFactory = new RobotFactory {
      var createdRobots = List[RobotId]()
      def apply(f: ActorRefFactory, robotId: RobotId) = {
        createdRobots = robotId :: createdRobots
        TestProbe().ref
      }
    }
    val dispatcher = TestFSMRef(new TrafficDispatcher(() => TrafficCondition.Light, robotFactory))
    val points = List(
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:40"),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:41"),
      RouteWaypoint(location = Location(51.487492,-0.095382), timestamp = "2011-03-22 08:01:42"),
      RouteWaypoint(location = Location(51.487545,-0.095404), timestamp = "2011-03-22 08:01:43")
    )

    "start in an initial state" in {
      dispatcher should have ('stateName (State.Initialised), 'stateData (Data.Empty))
    }
    "allow a set of waypoints to be configured" in {
      dispatcher ! Protocol.AddWaypoints(robotId = 5937, waypoints = points.toStream)
      dispatcher.stateData.asInstanceOf[Data.Waypoints].w.toList should contain theSameElementsInOrderAs points
    }
    "set up robots with the given IDs to patrol the town" in {
      robotFactory.createdRobots should contain only 5937
    }
    "should reply with a set of routes when requested" in {
      val fakeChild = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired)(fakeChild.ref)
      fakeChild.expectMsg(Protocol.VisitWaypoints(points))
      dispatcher.stateData.asInstanceOf[Data.Waypoints].w.toList should be (empty)
    }
  }
}

