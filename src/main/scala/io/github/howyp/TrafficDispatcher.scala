package io.github.howyp

import akka.actor.{FSM, Actor}
import io.github.howyp.TrafficDispatcher.Protocol

class TrafficDispatcher extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.State
  import TrafficDispatcher.Data

  startWith(State.Initialised, Data.Empty)

  when(State.Initialised) {
    case Event(Protocol.AddWaypointSource(newPoints), Data.Empty) => goto (State.Ready) using Data.Waypoints(newPoints)
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
    case class Waypoints(w: Iterator[RouteWaypoint]) extends Data
  }

  object Protocol {
    case class AddWaypointSource(s: Iterator[RouteWaypoint])
  }
}
