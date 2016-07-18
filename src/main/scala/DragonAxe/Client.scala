package DragonAxe

import java.io.PrintStream
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask

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
    connectRequestMessage.subscribe((body) => {
      val ip = body._1
      val nick = body._2

      val socket = new Socket(ip, 9999)

      val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
      val listenerFuture = Future[Try[String]] {
        new ClientListener(socket).call()
      }(ec)
      val senderFuture = Future[(String) => Unit](new ClientSender(socket).call())(ec)

      disconnectRequestMessage.subscribe((reason) => {

      })
    })
  }

}

/** Returns the function object that can be used to unsubscribe */
private class ClientSender(socket: Socket) extends Callable[(String) => Unit] {

  override def call(): (String) => Unit = {
    val out = new PrintStream(socket.getOutputStream())
    val func = (msg: String) => {
      out.println(msg)
    }
    pushServerMessage.subscribe(func)
    func
  }

}

/** Returns the reason for a server disconnect, or possibly and error */
private class ClientListener(socket: Socket) extends Callable[Try[String]] {

  val playerJoinedR = "pj=(.*)".r
  val playerLeftR = "pl=(.*)".r

  override def call(): Try[String] = Try {
    val in = new BufferedSource(socket.getInputStream()).getLines()
    try {
      /** Listens on socket input and returns disconnect reason */
      def reason: String = in.next() match {
        case "shutdown" => "Server stopped"
        case "kick" => "Kicked"
        case playerJoinedR(player) => playerJoinedMessage.publish(player)
          reason
        case playerLeftR(player) => playerLeftMessage.publish(player)
          reason
        case s: String => println("Unknown message: " + s)
          reason
      }
      reason
    } catch {
      case _ => "Lost connection"
    }
  }

}
