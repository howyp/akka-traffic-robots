package io.github.howyp

object Protocol {
  case class AddWaypoints(robotId: Robot.Id, waypoints: Stream[RouteWaypoint])
  case class MorePointsRequired(robotId: Robot.Id)
  case class VisitWaypoint(waypoint: RouteWaypoint)
  case object EndOfWaypointBatch
  case object Shutdown
  case class ShutdownComplete(robotId: Robot.Id)
}
