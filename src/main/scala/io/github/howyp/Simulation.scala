package io.github.howyp

import akka.actor.{ActorSystem, Props}

import scala.collection.immutable.IndexedSeq
import scala.io.Source

trait Simulation {
  def tubeStations: List[TubeStation]
  def waypointSource: Map[Robot.Id, Stream[RouteWaypoint]]
  def trafficConditionGenerator: () => TrafficCondition
  def system: ActorSystem

  def run() = {
    val dispatcherActor = system.actorOf(TrafficDispatcher.props(trafficConditionGenerator, tubeStations), name = "traffic-dispatcher")

    waypointSource.foreach {
      case (id, source) => dispatcherActor ! Protocol.AddWaypoints(id, source)
    }
    system.awaitTermination()
  }
}

object SimulationFromFiles extends App {
  private val robotIds = List(5937, 6043)

  private val simulation = new Simulation {
    val trafficConditionGenerator = TrafficCondition.random _
    val system = ActorSystem("traffic-robots")

    val tubeStations: List[TubeStation] =
      Source
        .fromFile(s"data/tube/tube.csv")
        .getLines()
        .map(TubeStation.parseLine)
        .map(_.getOrElse(throw new RuntimeException("Could not parse tube files")))
        .toList

    val waypointSource: Map[Robot.Id, Stream[RouteWaypoint]] =
      robotIds.map { id =>
        val waypoints =
          Source
            .fromFile(s"data/robot/$id.csv")
            .getLines()
            .map(RouteWaypoint.parseLine)
            .map(_.getOrElse(throw new RuntimeException("Could not parse route files")))
            .toStream
        id -> waypoints
      }(collection.breakOut)

    system.actorOf(Props[TrafficAnnouncer])
  }

  simulation.run()
}