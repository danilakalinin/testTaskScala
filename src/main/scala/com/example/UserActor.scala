package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object UserActor {
  sealed trait Command
  case class SendDirectMessage(message: String, to: ActorRef[ChatActor.Command]) extends Command
  case class ReceiveDirectMessage(message: String) extends Command

  def apply(controller: ChatController): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case SendDirectMessage(message, to) =>
        context.log.info(s"Sending direct message: $message")
        to ! ChatActor.ReceiveMessage(message)
        Behaviors.same

      case ReceiveDirectMessage(message) =>
        context.log.info(s"Direct message received: $message")
        controller.receiveMessage(message)
        Behaviors.same
    }
  }
}
