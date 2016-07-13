package DragonAxe

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent

import DragonAxe.RxIntegration._
import rx._

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.scene.control.ListView
import scalafx.scene.control.Separator
import scalafx.scene.control.TextField
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
  val playerList = new ListView[String]()
  val nickField = new TextField()
  val ipField = new TextField() {
    text = "localhost"
  }
  val connectToServerButton = new Button("Connect to Server") {
    maxWidth = Double.MaxValue
  }
  val startServerButton = new Button("Start Local Server") {
    maxWidth = Double.MaxValue
  }
  val statusLabel = new Label("Stopped") {
    maxWidth = Double.MaxValue
    alignment = Pos.CenterRight
  }
  val canvasCanvas = new Canvas(300, 300)

  // Define application logic
  nickField.text.rx().trigger {
    connectToServerButton.disable = nickField.text.isEmpty.get()
  }

  val connectionState = Var(false)
  val isConnected: Rx[Boolean] = Rx(connectionState())
  isConnected.trigger {
    val connected = isConnected.now
    nickField.disable = connected
    ipField.disable = connected
    if (connected == true) {
      connectToServerButton.text = "Disconnect"
    } else {
      connectToServerButton.text = "Connect to Server"
    }
  }

  connectToServerButton.setOnAction(new EventHandler[ActionEvent] {
    override def handle(t: ActionEvent): Unit = connectionState.update(!connectionState.now)
  })


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
              new Separator {
                prefHeight = 20
              },
              startServerButton,
              new BorderPane {
                left = new Label("Server status:")
                center = statusLabel
              }
            )
          }
        }
      }
    }
  }

}
