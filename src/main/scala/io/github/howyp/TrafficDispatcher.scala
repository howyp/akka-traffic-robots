package io.github.howyp

import akka.actor.{FSM, Props}
import io.github.howyp.Protocol.{EndOfWaypointBatch, VisitWaypoint}

class TrafficDispatcher(robotFactory: Robot.Factory) extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.{Data, State}

  startWith(State.Initialised, Data.Empty)

  when(State.Initialised) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Empty) =>
      robotFactory(context, robotId)
      goto (State.Ready) using Data.Waypoints(Map(robotId -> stream))
  }

  when(State.Ready) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Waypoints(w)) =>
      robotFactory(context, robotId)
      stay() using Data.Waypoints(w + (robotId -> stream))

    case Event(Protocol.MorePointsRequired(robotId), Data.Waypoints(waypoints)) =>
      waypoints.get(robotId) match {
        case None => stay()
        case Some(waypointsForRobot) => waypointsForRobot.splitAt (10) match {
          case (firstTen, remaining) =>
          for (point <- firstTen) sender () ! VisitWaypoint (point)
          sender () ! EndOfWaypointBatch
          stay () using Data.Waypoints (waypoints + (robotId -> remaining) )
        }
      }
  }
}
object TrafficDispatcher {
  def props(trafficConditionGenerator: () => TrafficCondition, tubeStations: List[TubeStation]) =
    Props.apply(new TrafficDispatcher(Robot.Factory(tubeStations, trafficConditionGenerator)))

  trait State
  object State {
    case object Initialised extends State
    case object Ready extends State
  }

  trait Data
  object Data {
    case object Empty extends Data
    case class Waypoints(waypoints: Map[RobotId, Stream[RouteWaypoint]]) extends Data
  }
}
