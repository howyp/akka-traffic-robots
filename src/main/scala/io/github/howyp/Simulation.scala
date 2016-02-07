package io.github.howyp

import akka.actor.{ActorSystem, Props}

import scala.io.Source

trait Simulation {
  def waypointSource: Stream[RouteWaypoint]
  def trafficConditionGenerator: () => TrafficCondition
  def system: ActorSystem

  def run() = {
    val announcerActor = system.actorOf(Props[TrafficAnnouncer])
    val dispatcherActor = system.actorOf(Props.apply(new TrafficDispatcher(trafficConditionGenerator)), "traffic-dispatcher")
    dispatcherActor ! TrafficDispatcher.Protocol.AddWaypointSource(waypointSource)
  }
}

object SimulationFromFiles extends App {
  private val robotIds = Stream(5937, 6043)

  val waypoints: Stream[RouteWaypoint] =
    robotIds
      .flatMap(id => Source.fromFile(s"data/robot/$id.csv").getLines().toStream)
      .map(RouteWaypoint.parseLine)
      .map(_.getOrElse(throw new RuntimeException("Could not parse route files")))

  private val self = new Simulation {
    val waypointSource = waypoints
    val trafficConditionGenerator = TrafficCondition.random _
    val system = ActorSystem("traffic-robots")
  }

  self.run()

  Thread.sleep(1000)
  self.system.shutdown()
}