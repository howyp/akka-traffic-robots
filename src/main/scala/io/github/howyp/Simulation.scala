package io.github.howyp

import akka.actor.{ActorSystem, Props}

trait Simulation {
  def waypointSource: Stream[RouteWaypoint]
  def trafficConditionSource: () => TrafficCondition
  def system: ActorSystem

  def run() = {
    val dispatcherActor = system.actorOf(Props[TrafficDispatcher], "traffic-dispatcher")
    dispatcherActor ! TrafficDispatcher.Protocol.AddWaypointSource(waypointSource)
  }
}

object Simulation extends App {
  private val robotIds = List(5937, 6043)
  val system = ActorSystem("traffic-robots")

  def waypointSource = ???

  def trafficConditionSource = ???

  system.awaitTermination()
}