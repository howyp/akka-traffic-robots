package io.github.howyp

import akka.actor.Props
import akka.testkit.{TestActorRef, TestProbe}
import io.github.howyp.test.actors.ActorSpec
import org.scalatest.{Matchers, FreeSpec}

class RobotSpec extends FreeSpec with Matchers with ActorSpec {
  "A Robot" - {
    "should request some waypoints from the dispatcher on startup" in {
      val dispatcher = TestProbe()
      val robot = TestActorRef(props = Props[Robot], supervisor = dispatcher.ref, name = "robot")
      dispatcher.expectMsg(TrafficDispatcher.Protocol.MorePointsRequired)
    }
  }
}
