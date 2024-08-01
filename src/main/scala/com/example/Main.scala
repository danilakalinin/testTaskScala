package com.example

import akka.actor.typed.{ActorSystem, SpawnProtocol, ActorRef}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext

object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[ChatApp], args: _*)
  }
}

class ChatApp extends Application {
  private var system: ActorSystem[SpawnProtocol.Command] = _

  override def start(primaryStage: Stage): Unit = {
    // Инициализация Akka System
    system = ActorSystem(SpawnProtocol(), "ChatSystem")

    // Определяем implicit значения для ask
    implicit val timeout: Timeout = 3.seconds
    implicit val scheduler = system.scheduler
    implicit val ec: ExecutionContext = system.executionContext

    val chatActorFuture = system.ask[ActorRef[ChatActor.Command]](SpawnProtocol.Spawn(ChatActor(), "chatActor", SpawnProtocol(), _))
    val userActorFuture = system.ask[ActorRef[UserActor.Command]](SpawnProtocol.Spawn(UserActor(new ChatController()), "userActor", SpawnProtocol(), _))

    // Обработка завершения создания актеров
    (for {
      chatActor <- chatActorFuture
      userActor <- userActorFuture
    } yield (chatActor, userActor)).onComplete {
      case Success((chatActor, userActor)) =>
        // Загрузка FXML и настройка UI
        val loader = new FXMLLoader(getClass.getResource("/fxml/main.fxml"))
        val root = loader.load[BorderPane]()
        val scene = new Scene(root)

        val controller = loader.getController[ChatController]
        controller.setActors(chatActor, userActor)

        primaryStage.setTitle("Chat Application")
        primaryStage.setScene(scene)
        primaryStage.show()

      case Failure(exception) =>
        exception.printStackTrace()
        system.terminate()
    }
  }

  override def stop(): Unit = {
    // Завершение работы ActorSystem при завершении приложения
    if (system != null) {
      system.terminate()
    }
  }
}
