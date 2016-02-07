package io.github

import akka.actor.{ActorRef, ActorRefFactory}

package object howyp {
  type RobotId = Int
  type RobotFactory = (ActorRefFactory, RobotId) => ActorRef
}
