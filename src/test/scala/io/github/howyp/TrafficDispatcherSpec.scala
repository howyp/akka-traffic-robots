package io.github.howyp

import akka.actor.ActorRefFactory
import akka.testkit.{TestFSMRef, TestProbe}
import io.github.howyp.test.actors.ActorSpec
import org.scalatest.{FreeSpec, Matchers}

class TrafficDispatcherSpec extends FreeSpec with Matchers with ActorSpec {
  import TrafficDispatcher._

  "The traffic dispatcher" - {

    val robotFactory = new Robot.Factory {
      var createdRobots = List[RobotId]()
      def apply(f: ActorRefFactory, robotId: RobotId) = {
        createdRobots = robotId :: createdRobots
        TestProbe().ref
      }
    }
    val dispatcher = TestFSMRef(new TrafficDispatcher(robotFactory))
    val points = List(
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:40"),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:41"),
      RouteWaypoint(location = Location(51.487492,-0.095382), timestamp = "2011-03-22 08:01:42"),
      RouteWaypoint(location = Location(51.487545,-0.095404), timestamp = "2011-03-22 08:01:43"),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:44"),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:45"),
      RouteWaypoint(location = Location(51.487492,-0.095382), timestamp = "2011-03-22 08:01:46"),
      RouteWaypoint(location = Location(51.487545,-0.095404), timestamp = "2011-03-22 08:01:47"),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:48"),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:49"),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = "2011-03-22 08:01:50"),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = "2011-03-22 08:01:51")
    )
    val robotId1 = 1234
    val robotId2 = 5678

    "start in an initial state" in {
      dispatcher should have ('stateName (State.Initialised), 'stateData (Data.Empty))
    }
    "allow a set of waypoints to be configured" in {
      dispatcher ! Protocol.AddWaypoints(robotId = robotId1, waypoints = points.toStream)
      dispatcher.stateData.asInstanceOf[Data.Waypoints].waypoints should be (Map(robotId1 -> points.toStream))
    }
    "set up robots with the given IDs to patrol the town" in {
      robotFactory.createdRobots should contain only robotId1
    }
    "should reply with a set of routes when requested, limited to the batch size" in {
      val testChildRobot = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId1))(testChildRobot.ref)
      testChildRobot.expectMsgAllOf(
        Protocol.VisitWaypoint(points(0)),
        Protocol.VisitWaypoint(points(1)),
        Protocol.VisitWaypoint(points(2)),
        Protocol.VisitWaypoint(points(3)),
        Protocol.VisitWaypoint(points(4)),
        Protocol.VisitWaypoint(points(5)),
        Protocol.VisitWaypoint(points(6)),
        Protocol.VisitWaypoint(points(7)),
        Protocol.VisitWaypoint(points(8)),
        Protocol.VisitWaypoint(points(9)),
        Protocol.EndOfWaypointBatch
      )
      dispatcher.stateData.asInstanceOf[Data.Waypoints].waypoints should be (
        Map(robotId1 -> Stream(points(10), points (11)))
      )
    }
    "should only reply with a routes for the requesting robot" in {
      val testChildRobot = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId2))(testChildRobot.ref)
      testChildRobot.expectNoMsg()
    }
    "should reply with a set of routes when requested, with only those that remain if less than the batch size" in {
      val testChildRobot = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId1))(testChildRobot.ref)
      testChildRobot.expectMsgAllOf(
        Protocol.VisitWaypoint(points(10)),
        Protocol.VisitWaypoint(points(11)),
        Protocol.EndOfWaypointBatch
      )
      dispatcher.stateData.asInstanceOf[Data.Waypoints].waypoints should be (Map(robotId1 -> Stream.empty))
    }
  }
}

