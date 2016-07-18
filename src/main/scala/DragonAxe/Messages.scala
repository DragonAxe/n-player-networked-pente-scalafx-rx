package DragonAxe

import scala.collection.mutable

/**
  * See http://jim-mcbeath.blogspot.com/2009/10/simple-publishsubscribe-example-in.html
  */
trait Publisher[E] {

  type S = (E) => Unit
  private var subscribers: List[S] = Nil

  private object lock

  def isSubscribed(subscriber: S): Boolean = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.contains(subscriber)
  }

  def subscribe(subscriber: S): Unit = lock.synchronized {
    if (!isSubscribed(subscriber)) {
      subscribers = subscribers :+ subscriber
    }
  }

  def unSubscribe(subscriber: S): Unit = lock.synchronized {
    subscribers = subscribers.filterNot((sub) => sub.equals(subscriber))
  }

  def publish(event: E): Unit = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.foreach((sub) => sub(event))
  }

}

/**
  * String1: ip address to connect to
  * String2: nickname to be known as to the server
  */
object connectRequestMessage extends Publisher[(String, String)]

/**
  * String: disconnect reason
  */
object disconnectMessage extends Publisher[String]

/**
  * String: message to send to server
  */
object pushServerMessage extends Publisher[String]

/**
  */
object disconnectRequestMessage extends Publisher[Null]

/**
  * String: nickname of the player who joined
  */
object playerJoinedMessage extends Publisher[String]

/**
  * String: nickname of the player who joined
  */
object playerLeftMessage extends Publisher[String]