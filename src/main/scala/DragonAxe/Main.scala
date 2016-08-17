package DragonAxe

import javafx.beans.binding.When
import javafx.beans.value.ObservableBooleanValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.stage.WindowEvent

import rx._
import DragonAxe.RxIntegration._

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.BooleanProperty
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

  // Veriables
  val isConnected = BooleanProperty(false)

  // Important components
  val playerList = new ListView[String]() {
    focusTraversable = false

  }
  val nickField = new TextField() {}
  val ipField = new TextField() {
    text = "localhost"
  }
  val connectToServerButton = new Button("Connect to Server") {
    maxWidth = Double.MaxValue
    disable <== nickField.text.length.greaterThan(0).not() or ipField.text.length.greaterThan(0).not()
    text <== when(isConnected) choose "Disconnected" otherwise "Connect to Server"
    onAction = new EventHandler[ActionEvent] {
      override def handle(t: ActionEvent): Unit = {
        if (isConnected.value) {
          isConnected.set(false)
        } else {
          isConnected.set(true)
        }
      }
    }
  }
  val connectionStatusLabel = new Label("Disconnected") {
    maxWidth = Double.MaxValue
    alignment = Pos.CenterRight
  }
  val playerReadyCheckbox = new CheckBox() {
    disable <== isConnected.not()
    onAction = new EventHandler[ActionEvent] {
      override def handle(t: ActionEvent): Unit = {

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
    override def handle(t: WindowEvent): Unit = {
      //      disconnectRequestMessage.publish()
    }
  })

}
