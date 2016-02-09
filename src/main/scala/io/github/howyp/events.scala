package io.github.howyp

import java.time.LocalDateTime

import scala.util.Random

trait SimulationEvent { val robotId: RobotId }
case class TrafficReport(robotId: RobotId, timestamp: LocalDateTime, speed: Double, condition: TrafficCondition) extends SimulationEvent
case class RobotMoved(robotId: RobotId, newLocation: Location) extends SimulationEvent

trait TrafficCondition
object TrafficCondition {
  def random(): TrafficCondition = Random.nextInt(3) match {
    case 0 => Light
    case 1 => Medium
    case 2 => Heavy
  }

  case object Light extends TrafficCondition
  case object Medium extends TrafficCondition
  case object Heavy extends TrafficCondition
}