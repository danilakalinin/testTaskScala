package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object ChatActor {
  sealed trait Command
  case class SendMessage(message: String, replyTo: ActorRef[Response]) extends Command
  case class ReceiveMessage(message: String) extends Command
  case class JoinGroup(user: ActorRef[UserActor.Command]) extends Command
  case class LeaveGroup(user: ActorRef[UserActor.Command]) extends Command

  sealed trait Response
  case class MessageSent(status: String) extends Response

  def apply(group: Set[ActorRef[UserActor.Command]] = Set.empty): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case SendMessage(message, replyTo) =>
        context.log.info(s"Received message: $message")
        replyTo ! MessageSent("Message sent")
        group.foreach { user =>
          user ! UserActor.ReceiveDirectMessage(message)
        }
        Behaviors.same

      case ReceiveMessage(message) =>
        context.log.info(s"Message received: $message")
        Behaviors.same

      case JoinGroup(user) =>
        context.log.info(s"User joined group: $user")
        apply(group + user)

      case LeaveGroup(user) =>
        context.log.info(s"User left group: $user")
        apply(group - user)
    }
  }
}
