package io.github.howyp

import akka.actor.FSM
import io.github.howyp.TrafficDispatcher.Protocol
import io.github.howyp.TrafficDispatcher.Protocol.{MorePointsRequired, VisitWaypoints}

class TrafficDispatcher(trafficConditionGenerator: () => TrafficCondition, robotFactory: RobotFactory) extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.{Data, State}

  startWith(State.Initialised, Data.Empty)

  when(State.Initialised) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Empty) =>
      robotFactory(context, robotId)
      context.system.eventStream.publish(TrafficReport(1, stream.head.timestamp, 30, TrafficCondition.Light))
      goto (State.Ready) using Data.Waypoints(stream)
  }

  when(State.Ready) {
    case Event(MorePointsRequired, Data.Waypoints(stream)) =>
      stream.splitAt(10) match { case (firstTen, remaining) =>
        sender() ! VisitWaypoints(firstTen.toList)
        stay() using Data.Waypoints(remaining )
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

  object Protocol {
    case class AddWaypoints(robotId: RobotId, waypoints: Stream[RouteWaypoint])
    case object MorePointsRequired
    case class VisitWaypoints(waypoints: List[RouteWaypoint])
  }
}
