package io.github.howyp

import java.time.LocalDateTime

import akka.actor.ActorRefFactory
import akka.testkit.{TestFSMRef, TestProbe}
import io.github.howyp.test.actors.ActorSpec
import org.scalatest.{FreeSpec, Matchers}

class TrafficDispatcherSpec extends FreeSpec with Matchers with ActorSpec {
  import TrafficDispatcher._

  "The traffic dispatcher" - {

    val robotFactory = new Robot.Factory {
      var createdRobots = List[Robot.Id]()
      def apply(f: ActorRefFactory, robotId: Robot.Id) = {
        createdRobots = robotId :: createdRobots
        TestProbe().ref
      }
    }
    val dispatcher = TestFSMRef(new TrafficDispatcher(robotFactory))
    val `waypointAfter8:10AM` = RouteWaypoint(location = Location(51.487381, -0.095346), timestamp = LocalDateTime.parse("2011-03-22T08:10:00"))
    val points = List(
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = LocalDateTime.parse("2011-03-22T08:01:40")),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = LocalDateTime.parse("2011-03-22T08:01:41")),
      RouteWaypoint(location = Location(51.487492,-0.095382), timestamp = LocalDateTime.parse("2011-03-22T08:01:42")),
      RouteWaypoint(location = Location(51.487545,-0.095404), timestamp = LocalDateTime.parse("2011-03-22T08:01:43")),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = LocalDateTime.parse("2011-03-22T08:01:44")),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = LocalDateTime.parse("2011-03-22T08:01:45")),
      RouteWaypoint(location = Location(51.487492,-0.095382), timestamp = LocalDateTime.parse("2011-03-22T08:01:46")),
      RouteWaypoint(location = Location(51.487545,-0.095404), timestamp = LocalDateTime.parse("2011-03-22T08:01:47")),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = LocalDateTime.parse("2011-03-22T08:01:48")),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = LocalDateTime.parse("2011-03-22T08:01:49")),
      RouteWaypoint(location = Location(51.487381,-0.095346), timestamp = LocalDateTime.parse("2011-03-22T08:01:50")),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = LocalDateTime.parse("2011-03-22T08:01:51")),
      RouteWaypoint(location = Location(51.487434,-0.095362), timestamp = LocalDateTime.parse("2011-03-22T08:08:59"))
    )
    val robotId1 = 1234
    val robotId2 = 5678

    "starts in an initial state" in {
      dispatcher should have ('stateName (State.Initialised), 'stateData (Data.Empty))
    }
    "allows a set of waypoints to be configured" in {
      dispatcher ! Protocol.AddWaypoints(robotId = robotId1, waypoints = points.toStream #::: Stream(`waypointAfter8:10AM`))
      dispatcher.stateData.asInstanceOf[Data.Waypoints].waypoints should be (Map(robotId1 -> points.toStream))
    }
    "sets up robots with the given IDs to patrol the town" in {
      robotFactory.createdRobots should contain only robotId1
    }
    "replies with a set of routes when requested, limited to the batch size of 10" in {
      val testChildRobot1 = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId1))(testChildRobot1.ref)
      testChildRobot1.expectMsgAllOf(
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
        Map(robotId1 -> Stream(points(10), points (11), points (12)))
      )
    }
    "only replies with a routes for the requesting robot" in {
      val testChildRobot2 = TestProbe()
      val pointsForRobot2 = points.map(p => p.copy(location = Location(p.location.latitude + 1, 0)))
      dispatcher ! Protocol.AddWaypoints(robotId = robotId2, waypoints = pointsForRobot2.toStream)
      dispatcher.!(Protocol.MorePointsRequired(robotId2))(testChildRobot2.ref)
      testChildRobot2.expectMsgAllOf(
        Protocol.VisitWaypoint(pointsForRobot2(0)),
        Protocol.VisitWaypoint(pointsForRobot2(1)),
        Protocol.VisitWaypoint(pointsForRobot2(2)),
        Protocol.VisitWaypoint(pointsForRobot2(3)),
        Protocol.VisitWaypoint(pointsForRobot2(4)),
        Protocol.VisitWaypoint(pointsForRobot2(5)),
        Protocol.VisitWaypoint(pointsForRobot2(6)),
        Protocol.VisitWaypoint(pointsForRobot2(7)),
        Protocol.VisitWaypoint(pointsForRobot2(8)),
        Protocol.VisitWaypoint(pointsForRobot2(9)),
        Protocol.EndOfWaypointBatch
      )
    }
    "replies with a set of routes when requested, with only those that remain that are before 8:10 AM, if less than the batch size" in {
      val testChildRobot1 = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId1))(testChildRobot1.ref)
      testChildRobot1.expectMsgAllOf(
        Protocol.VisitWaypoint(points(10)),
        Protocol.VisitWaypoint(points(11)),
        Protocol.VisitWaypoint(points(12)),
        Protocol.EndOfWaypointBatch
      )
      dispatcher.stateData.asInstanceOf[Data.Waypoints].waypoints(robotId1) should be (empty)
    }
    "tells a robot to shut down if more routes are requested but none remain for it" in {
      val testChildRobot1 = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId1))(testChildRobot1.ref)
      testChildRobot1.expectMsg(Protocol.Shutdown)
    }
    "terminates the simulation after all robots have shutdown" in {
      val testChildRobot1 = TestProbe()
      dispatcher.!(Protocol.MorePointsRequired(robotId2))(testChildRobot1.ref)
      dispatcher.!(Protocol.MorePointsRequired(robotId2))(testChildRobot1.ref)

      dispatcher ! Protocol.ShutdownComplete(robotId1)
      dispatcher ! Protocol.ShutdownComplete(robotId2)

      val watcher = TestProbe()
      watcher.watch(dispatcher)
      watcher.expectTerminated(dispatcher)
    }
  }
}

