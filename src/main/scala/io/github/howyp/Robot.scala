package io.github.howyp

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}

class Robot(id: RobotId, tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition) extends Actor {

  val stationsByLocation: Map[Location, TubeStation] =
    tubeStations.map(s => s.location -> s)(collection.breakOut)

  override def preStart() {
    context.parent ! TrafficDispatcher.Protocol.MorePointsRequired(id)
  }

  def receive = {
    case TrafficDispatcher.Protocol.VisitWaypoint(RouteWaypoint(timestamp, newLocation)) =>
      context.system.eventStream.publish(RobotMoved(newLocation))
      stationsByLocation.get(newLocation) foreach { station =>
        context.system.eventStream.publish(TrafficReport(id, timestamp, 0, trafficConditionGenerator()))
      }
    case TrafficDispatcher.Protocol.EndOfWaypointBatch =>
      context.parent ! TrafficDispatcher.Protocol.MorePointsRequired(id)
  }
}
object Robot {
  def props(id: RobotId, tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition) =
    Props.apply(new Robot(id, tubeStations, trafficConditionGenerator))

  //TODO can this be replaced by just injecting props?
  type Factory = (ActorRefFactory, RobotId) => ActorRef
  object Factory {
    def apply(tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition): Factory =
      (f, id) => { f.actorOf(props(id, tubeStations, trafficConditionGenerator), id.toString) }
  }
}
