package com.example

import akka.actor.{Props, ActorSystem}
import io.github.howyp.{TrafficDispatcher, RouteWaypoint}

import scala.io.Source

object ApplicationMain extends App {
  private val robotIds = List(5937, 6043)

  val system = ActorSystem("traffic-robots")
  val dispatcherActor = system.actorOf(Props[TrafficDispatcher], "traffic-dispatcher")
  system.awaitTermination()
}