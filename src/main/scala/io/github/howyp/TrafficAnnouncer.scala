package io.github.howyp

import akka.actor.Actor

class TrafficAnnouncer extends Actor {
  context.system.eventStream.subscribe(self, classOf[TrafficReport])

  def receive = {
    case a: TrafficReport => println(a)
  }
}
