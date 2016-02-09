package io.github.howyp

import akka.actor.Actor

class TrafficAnnouncer extends Actor {
  context.system.eventStream.subscribe(self, classOf[SimulationEvent])

  var eventCounter = 0

  def receive = { case a =>
    println(s"$eventCounter. $a")
    eventCounter = eventCounter + 1
  }
}
