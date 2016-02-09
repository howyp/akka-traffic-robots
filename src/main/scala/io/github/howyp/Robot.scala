package io.github.howyp

import java.time.ZoneOffset

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.event.LoggingReceive

class Robot(id: Robot.Id, tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition) extends Actor {

  override def preStart() {
    context.parent ! Protocol.MorePointsRequired(id)
  }

  var lastLocation: Option[RouteWaypoint] = None

  def receive = LoggingReceive {

    case Protocol.VisitWaypoint(waypoint) =>
      val speed = moveTo(waypoint)
      tubeStations.find(_.location.distanceInMeters(waypoint.location) < 350) foreach { station =>
        context.system.eventStream.publish(TrafficReport(id, waypoint.timestamp, speed, trafficConditionGenerator()))
      }
      lastLocation = Some(waypoint)

    case Protocol.EndOfWaypointBatch =>
      context.parent ! Protocol.MorePointsRequired(id)

    case Protocol.Shutdown =>
      sender() ! Protocol.ShutdownComplete(id)
      context.stop(self)
  }

  def moveTo(newPoint: RouteWaypoint): Double = {
    context.system.eventStream.publish(RobotMoved(id, newPoint.location))
    lastLocation match {
      case None => 0
      case Some(RouteWaypoint(oldTimestamp, oldLocation)) =>
        oldLocation.distanceInMeters(newPoint.location) /
          (newPoint.timestamp.toEpochSecond(ZoneOffset.UTC) - oldTimestamp.toEpochSecond(ZoneOffset.UTC))
    }
  }
}
object Robot {
  def props(id: Robot.Id, tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition) =
    Props.apply(new Robot(id, tubeStations, trafficConditionGenerator))

  type Id = Int

  //TODO can this be replaced by just injecting props?
  type Factory = (ActorRefFactory, Robot.Id) => ActorRef
  object Factory {
    def apply(tubeStations: List[TubeStation], trafficConditionGenerator: () => TrafficCondition): Factory =
      (f, id) => { f.actorOf(props(id, tubeStations, trafficConditionGenerator).withDispatcher("dispatcherWithOneThreadPerActor"), id.toString) }
  }
}
