package com.example

import javafx.fxml.FXML
import javafx.scene.control.{TextArea, TextField}
import javafx.event.ActionEvent
import akka.actor.typed.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.SpawnProtocol

class ChatController {

  @FXML
  private var chatArea: TextArea = _

  @FXML
  private var messageInput: TextField = _

  private var chatActor: ActorRef[ChatActor.Command] = _
  private var userActor: ActorRef[UserActor.Command] = _

  def setActors(chatActor: ActorRef[ChatActor.Command], userActor: ActorRef[UserActor.Command]): Unit = {
    this.chatActor = chatActor
    this.userActor = userActor
  }

  @FXML
  def initialize(): Unit = {
    // Инициализация контроллера, если необходимо
  }

  @FXML
  def sendMessage(event: ActionEvent): Unit = {
    val message = messageInput.getText
    if (message.nonEmpty) {
      chatArea.appendText(s"You: $message\n")
      messageInput.clear() // Очистка поля ввода после отправки
      if (userActor != null && chatActor != null) {
        userActor ! UserActor.SendDirectMessage(message, chatActor)
      }
    }
  }

  def receiveMessage(message: String): Unit = {
    chatArea.appendText(s"Other: $message\n")
  }
}
