package DragonAxe

import scala.collection.mutable

trait Publisher {
  type S

  protected var subscribers = mutable.HashMap.empty[Int, S]
  protected var singleSubscribers: List[S] = Nil

  protected object lock

  def isSubscribed(subID: Int): Boolean = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.contains(subID)
  }

  def subscribe(subscriber: S): Int = lock.synchronized {
    val id = subscribers.size
    if (!isSubscribed(id)) {
      subscribers = subscribers.+=((id, subscriber))
    }
    println("SubID:" + id)
    id
  }

  def subscribeOnce(subscriber: S): Unit = lock.synchronized {
    singleSubscribers = singleSubscribers :+ subscriber
  }

  def unSubscribe(subID: Int): Unit = lock.synchronized {
    subscribers.remove(subID)
  }
}

trait Publisher0 extends Publisher {

  type S = () => Unit

  def publish(): Unit = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.foreach(body => body._2())
    singleSubscribers = Nil
  }

}

/**
  * See http://jim-mcbeath.blogspot.com/2009/10/simple-publishsubscribe-example-in.html
  */
trait Publisher1[E] extends Publisher {

  type S = (E) => Unit

  def publish(event: E): Unit = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.foreach(body => body._2(event))
    singleSubscribers = Nil
  }

}

trait Publisher2[E, F] extends Publisher {

  type S = (E, F) => Unit

  def publish(event1: E, event2: F): Unit = {
    val subs = lock.synchronized {
      subscribers
    }
    subs.foreach(body => body._2(event1, event2))
    singleSubscribers = Nil
  }

}

/**
  * String1: ip address to connect to
  * String2: nickname to be known as to the server
  */
object connectRequestMessage extends Publisher2[String, String]

/**
  * String: disconnect reason
  */
object disconnectMessage extends Publisher1[String]

/**
  * String: message to send to server
  */
object pushServerMessage extends Publisher1[String]

/**
  */
object disconnectRequestMessage extends Publisher0

/**
  * String: nickname of the player who joined
  */
object playerJoinedMessage extends Publisher1[String]

/**
  * String: nickname of the player who joined
  */
object playerLeftMessage extends Publisher1[String]
