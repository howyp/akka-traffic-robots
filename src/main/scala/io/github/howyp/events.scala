package io.github.howyp

trait SimulationEvent { val robotId: RobotId }
case class TrafficReport(robotId: RobotId, timestamp: String, speed: Int, condition: TrafficCondition) extends SimulationEvent
case class RobotMoved(robotId: RobotId, newLocation: Location) extends SimulationEvent
