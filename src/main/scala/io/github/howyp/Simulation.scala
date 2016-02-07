package io.github.howyp

import akka.actor.{ActorSystem, Props}

import scala.collection.immutable.IndexedSeq
import scala.io.Source

trait Simulation {
  def waypointSource: Map[RobotId, Stream[RouteWaypoint]]
  def trafficConditionGenerator: () => TrafficCondition
  def system: ActorSystem

  def run() = {
    system.actorOf(Props[TrafficAnnouncer])

    val dispatcherActor = system.actorOf(Props.apply(
      new TrafficDispatcher(trafficConditionGenerator, (f, id) => f.actorOf(Props[Robot], id.toString))
    ), "traffic-dispatcher")

    waypointSource.foreach {
      case (id, source) => dispatcherActor ! TrafficDispatcher.Protocol.AddWaypoints(id, source)
    }
  }
}

object SimulationFromFiles extends App {
  private val robotIds = List(5937, 6043)

  val waypoints: Map[RobotId, Stream[RouteWaypoint]] = robotIds.map { id =>
    (id, Source.fromFile(s"data/robot/$id.csv").getLines().toStream
               .map(RouteWaypoint.parseLine)
               .map(_.getOrElse(throw new RuntimeException("Could not parse route files"))))
  } (collection.breakOut)

  private val self = new Simulation {
    val waypointSource = waypoints
    val trafficConditionGenerator = TrafficCondition.random _
    val system = ActorSystem("traffic-robots")
  }

  self.run()

  Thread.sleep(1000)
  self.system.shutdown()
}