package DragonAxe

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import rx._
import DragonAxe.RxIntegration._

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.ColumnConstraints
import scalafx.scene.layout.GridPane
import scalafx.scene.layout.Priority
import scalafx.scene.layout.VBox

/**
  * The main GUI application.
  */
object Main extends JFXApp {

  // Important components
  val playerList = new ListView[String]() {
    focusTraversable = false
    // Update list of players when player joins server
    playerJoinedMessage.subscribe((name) => {
      Platform.runLater(items.get().add(name))
    })
    // Update list of players when player leaves server
    playerLeftMessage.subscribe((name) => {
      Platform.runLater(items.get().remove(name))
    })
    disconnectMessage.subscribe((reason) => {
      Platform.runLater(items.get().retainAll())
    })
  }
  val nickField = new TextField() {
    connectRequestMessage.subscribe((ip, nick) => {
      disable = true
    })
    disconnectMessage.subscribe((reason) => {
      disable = false
    })
  }
  val ipField = new TextField() {
    text = "localhost"
    connectRequestMessage.subscribe((ip, nick) => {
      disable = true
    })
    disconnectMessage.subscribe((reason) => {
      disable = false
    })
  }
  val connectToServerButton = new Button("Connect to Server") {
    maxWidth = Double.MaxValue
    var isConnected = false
    connectRequestMessage.subscribe((ip, nick) => {
      isConnected = true
      text = "Disconnect"
    })
    disconnectMessage.subscribe((reason) => {
      isConnected = false
      Platform.runLater(text = "Connect to Server")
    })
    onAction = new EventHandler[ActionEvent] {
      override def handle(t: ActionEvent): Unit = {
        if (!isConnected) {
          connectRequestMessage.publish(ipField.text.value, nickField.text.value)
        } else {
          disconnectRequestMessage.publish()
          disconnectMessage.publish("Left server")
        }
      }
    }
  }
  val connectionStatusLabel = new Label("Disconnected") {
    maxWidth = Double.MaxValue
    alignment = Pos.CenterRight
    connectRequestMessage.subscribe((ip, nick) => {
      text = "Connected"
    })
    disconnectMessage.subscribe((reason) => {
      Platform.runLater(text = reason)
    })
  }
  val playerReadyCheckbox = new CheckBox() {
    disable = true
    connectRequestMessage.subscribe((ip, nick) => {
      disable = false
    })
    disconnectMessage.subscribe((reason) => {
      disable = true
    })
    onAction = new EventHandler[ActionEvent] {
      override def handle(t: ActionEvent): Unit = {
        pushServerMessage.publish("ready=" + selected.value.toString)
      }
    }
  }
  val startServerButton = new Button("Start Local Server") {
    maxWidth = Double.MaxValue
  }
  val startGameButton = new Button("Start game") {
    maxWidth = Double.MaxValue
    disable = true
    margin = Insets(6, 0, 3, 0)
  }
  val serverStatusLabel = new Label("Stopped") {
    maxWidth = Double.MaxValue
    alignment = Pos.CenterRight
  }
  val canvasCanvas = new Canvas(300, 300)


  //// Define application logic //
  // Disable connect to server button if nick or ip inputField are empty
  {
    val textField1 = nickField.text.rx()
    val textField2 = ipField.text.rx()
    val isEmpty = Rx {
      textField1().isEmpty || textField2().isEmpty
    }
    isEmpty.trigger(connectToServerButton.setDisable(isEmpty.now))
  }

  Client.init()


  // Define the layout
  stage = new PrimaryStage {
    title = "N-player connect five"
    scene = new Scene {
      root = new BorderPane {
        padding = Insets(6)
        center = canvasCanvas
        left = new BorderPane {
          top = new Label("Players:") {
            maxWidth = Double.MaxValue
            alignment = Pos.Center
          }
          center = playerList
          bottom = new VBox() {
            children = List(
              new GridPane {
                add(new Label("Nick: "), 0, 0)
                add(nickField, 1, 0)
                add(new Label("IP: "), 0, 1)
                add(ipField, 1, 1)
                add(new Label("Ready? "), 0, 2)
                add(playerReadyCheckbox, 1, 2)
                padding = Insets(6, 0, 6, 0)
                vgap = 6
                columnConstraints = List(
                  new ColumnConstraints(),
                  new ColumnConstraints {
                    hgrow = Priority.Always
                  }
                )
              },
              connectToServerButton,
              new BorderPane {
                left = new Label("Client status:")
                center = connectionStatusLabel
              },
              new Separator {
                prefHeight = 20
              },
              startServerButton,
              startGameButton,
              new BorderPane {
                left = new Label("Server status:")
                center = serverStatusLabel
              }
            )
          }
        }
      }
    }
  }

  stage.onCloseRequest.setValue(new EventHandler[WindowEvent] {
    override def handle(t: WindowEvent): Unit = disconnectRequestMessage.publish()
  })

}
