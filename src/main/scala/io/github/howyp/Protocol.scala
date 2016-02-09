package io.github.howyp

object Protocol {
  case class AddWaypoints(robotId: RobotId, waypoints: Stream[RouteWaypoint])
  case class MorePointsRequired(robotId: RobotId)
  case class VisitWaypoint(waypoint: RouteWaypoint)
  case object EndOfWaypointBatch
}
