package io.github.howyp

import akka.actor.{Props, ActorRef, ActorRefFactory, Actor}

class Robot(tubeStations: List[TubeStation]) extends Actor {

  //FIXME remove
  context.system.eventStream.publish(TrafficReport(1, "1", 30, TrafficCondition.Light))

  override def preStart() {
    context.parent ! TrafficDispatcher.Protocol.MorePointsRequired
  }

  def receive = {
    case TrafficDispatcher.Protocol.VisitWaypoint(RouteWaypoint(_, newLocation)) =>
      context.system.eventStream.publish(RobotMoved(newLocation))
  }
}
object Robot {
  def props(tubeStations: List[TubeStation]) = Props.apply(new Robot(tubeStations))

  type Factory = (ActorRefFactory, RobotId) => ActorRef
  object Factory {
    def apply(tubeStations: List[TubeStation]): Factory = (f, id) => { f.actorOf(props(tubeStations), id.toString) }
  }
}
