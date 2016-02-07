package io.github.howyp

import akka.actor.{FSM, Actor}
import io.github.howyp.TrafficDispatcher.Protocol

class TrafficDispatcher extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.State
  import TrafficDispatcher.Data

  startWith(State.Initialised, Data.Empty)

  when(State.Initialised) {
    case Event(Protocol.AddWaypointSource(stream), Data.Empty) =>
      context.system.eventStream.publish(TrafficReport(stream.head.robotId, stream.head.timestamp, 30, TrafficCondition.Light))
      goto (State.Ready) using Data.Waypoints(stream)
  }

  when(State.Ready) { case _ => ??? }
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
    case class AddWaypointSource(s: Stream[RouteWaypoint])
  }
}
