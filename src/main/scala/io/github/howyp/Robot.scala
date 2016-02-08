package io.github.howyp

import akka.actor.Actor

class Robot extends Actor {


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
