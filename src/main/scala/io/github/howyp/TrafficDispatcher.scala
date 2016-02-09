package io.github.howyp

import akka.actor.{FSM, Props}
import akka.event.LoggingReceive
import io.github.howyp.Protocol.{EndOfWaypointBatch, VisitWaypoint}

class TrafficDispatcher(robotFactory: Robot.Factory) extends FSM[TrafficDispatcher.State, TrafficDispatcher.Data] {
  import TrafficDispatcher.{Data, State}

  startWith(State.Initialised, Data.Empty)


  when(State.Initialised) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Empty) =>
      robotFactory(context, robotId)
      goto (State.Ready) using Data.Waypoints(Map(robotId -> stream.filter(`isBefore8:10AM`)))
  }

  when(State.Ready) {
    case Event(Protocol.AddWaypoints(robotId, stream), Data.Waypoints(w)) =>
      robotFactory(context, robotId)
      stay() using Data.Waypoints(w + (robotId -> stream))

    case Event(Protocol.ShutdownComplete(robotId), waypoints: Data.Waypoints) =>
      waypoints.remove(robotId) match {
        case remaining: Data.Waypoints => stay() using remaining
        case Data.Empty => stop()
      }

    case Event(Protocol.MorePointsRequired(robotId), waypoints: Data.Waypoints) =>
      waypoints.get(robotId) match {
        case None => stay()
        case Some(waypointsForRobot) => waypointsForRobot.splitAt (10) match {
          case (Stream.Empty, _) =>
            stay() replying Protocol.Shutdown
          case (firstTen, remaining) =>
            for (point <- firstTen) sender () ! VisitWaypoint (point)
            stay() replying EndOfWaypointBatch using waypoints.update(robotId, remaining)
        }
      }
  }

  def `isBefore8:10AM`(w: RouteWaypoint) = {
    val t = w.timestamp
    t.getHour < 8 || (t.getHour == 8 && t.getMinute < 10)
  }

  override def receive = LoggingReceive(super.receive)
}
trait ShutdownSystemOnTermination { this: TrafficDispatcher =>
  onTermination { case _ =>
    // Allow time for the logger to complete
    Thread.sleep(1000)
    context.system.shutdown()
  }
}
object TrafficDispatcher {
  def props(trafficConditionGenerator: () => TrafficCondition, tubeStations: List[TubeStation]) =
    Props.apply(new TrafficDispatcher(Robot.Factory(tubeStations, trafficConditionGenerator)) with ShutdownSystemOnTermination)

  trait State
  object State {
    case object Initialised extends State
    case object Ready extends State
  }

  trait Data
  object Data {
    case object Empty extends Data
    case class Waypoints(waypoints: Map[Robot.Id, Stream[RouteWaypoint]]) extends Data {
      val get = waypoints.get _
      def isEmpty = waypoints.isEmpty
      def remove(robotId: Robot.Id) = {
        val w = Waypoints(waypoints.-(robotId))
        if (w.isEmpty) Data.Empty else w
      }
      def update(robotId: Robot.Id, newPointsForRobot: Stream[RouteWaypoint]) =
        Waypoints(waypoints + (robotId -> newPointsForRobot))
    }
  }
}
