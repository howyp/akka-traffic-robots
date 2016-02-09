package io.github.howyp

import java.time.LocalDateTime

trait SimulationEvent { val robotId: RobotId }
case class TrafficReport(robotId: RobotId, timestamp: LocalDateTime, speed: Int, condition: TrafficCondition) extends SimulationEvent
case class RobotMoved(robotId: RobotId, newLocation: Location) extends SimulationEvent
