package io.github.howyp.test.actors

import akka.actor.ActorSystem
import akka.testkit.TestProbe

trait EventStreamListening {
  implicit def system: ActorSystem
  val eventStream = TestProbe()

  def listenOnEventStreamFor(c: Class[_]) = system.eventStream.subscribe(eventStream.ref, c)
}
