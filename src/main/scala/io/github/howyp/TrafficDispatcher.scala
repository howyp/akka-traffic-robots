package io.github.howyp

import akka.actor.FSM
import io.github.howyp.TrafficDispatcher.Protocol
import io.github.howyp.TrafficDispatcher.Protocol.{MorePointsRequired, VisitWaypoint}

class TrafficDispatcher(trafficConditionGenerator: () => TrafficCondition, robotFactory: RobotFactory) extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.{Data, State}

  startWith(State.Initialised, Data.Empty)

  when(State.Initialised) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Empty) =>
      robotFactory(context, robotId)
      goto (State.Ready) using Data.Waypoints(stream)
  }

  when(State.Ready) {
    case Event(MorePointsRequired, Data.Waypoints(stream)) =>
      stream.splitAt(10) match { case (firstTen, remaining) =>
        for (point <- firstTen) sender() ! VisitWaypoint(point)
        stay() using Data.Waypoints(remaining)
      }
  }
}
object TrafficDispatcher {
  trait State
  object State {
    case object Initialised extends State
    case object Ready extends State
  }

  trait Data
  object Data {
    case object Empty extends Data
    case class Waypoints(w: Stream[RouteWaypoint]) extends Data
  }

  //TODO maybe extract outside?
  object Protocol {
    case class AddWaypoints(robotId: RobotId, waypoints: Stream[RouteWaypoint])
    case object MorePointsRequired
    case class VisitWaypoint(waypoint: RouteWaypoint)
  }
}
