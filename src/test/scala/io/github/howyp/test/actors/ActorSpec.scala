package io.github.howyp.test.actors

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.Suite

import scala.concurrent.duration.FiniteDuration

trait ActorSpec { this: Suite =>

  implicit lazy val system = ActorSystem("test")

  val timeoutDuration = FiniteDuration(3, TimeUnit.SECONDS)
  implicit val timeout = Timeout(timeoutDuration)
}
