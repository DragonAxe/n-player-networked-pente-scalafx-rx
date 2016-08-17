package DragonAxe

import java.io.PrintStream
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.io.BufferedSource
import scala.util.Try

/*
 * Send a disconnectMessage(Reason for disconnect) if program disconnects from server for any reason.
 * Disconnect from server at any time if disconnectRequestMessage is received.
 * Asynchronously send out messages received from the server. (Can also receive disonnect request from server)
 * Asynchronously transmit messages from the gui to the server.
 * Connect to server when connectMessage(ip, nickname) is received, but only if not already connected.
 */

object Client {

  def init(): Unit = {
    // Start server if connect message received

    val socket = new Socket("localhost", 9999)

    println("Working?")


    val out = new PrintStream(socket.getOutputStream())


    new ClientListener(socket).start()


  }
}

/** Returns the function object that can be used to unsubscribe */
private class ClientListener(socket: Socket) extends Thread {

  val playerJoinedR = "pj=(.*)".r
  val playerLeftR = "pl=(.*)".r

  def listenThenReason(): Try[String] = Try {
    val in = new BufferedSource(socket.getInputStream()).getLines()
    try {
      /** Listens on socket input and returns disconnect reason */
      def reason: String = in.next() match {
        case "shutdown" => "Server stopped"
        case "kick" => "Kicked"
        //        case playerJoinedR(player) => playerJoinedMessage.publish(player)
        //          reason
        //        case playerLeftR(player) => playerLeftMessage.publish(player)
        //          reason
        case s: String => println("Unknown message: " + s)
          reason
      }
      reason
    } catch {
      case e: SocketException => "Left server"
      case e: NoSuchElementException => "Server was stopped"
    }
  }

  override def run(): Unit = {
    val reason = listenThenReason()
    val rStr: String = reason.getOrElse("Er! " + reason.failed.get.getMessage)
    reason.getOrElse(reason.failed.get.printStackTrace())
    //    disconnectMessage.publish(rStr)
  }
}
