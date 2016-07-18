package DragonAxe

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

import rx._

object RxIntegration {
  val observers: scala.collection.mutable.Buffer[Obs] = scala.collection.mutable.Buffer()

  implicit class PropertyExtensions[T, J](p: scalafx.beans.property.Property[T, J]) {
    //    def |=(x: => T): Unit = {
    //      //      val rx = Rx {x}
    //      //      observers += Obs(rx) { p() = rx() }
    //    }
    //
    //    def pipeFrom(x: => T)(implicit ctx: Ctx.Owner): Unit = {
    //      val rx = Rx {
    //        x
    //      }
    //      observers += rx.trigger(p.update(rx))
    //    }
  }

  implicit class ReadOnlyPropertyExtensions[T, J](p: scalafx.beans.property.ReadOnlyProperty[T, J]) {
    def rx(): Rx[T] = {
      val v = Var(p.value)
      p.addListener(new ChangeListener[J] {
        override def changed(observable: ObservableValue[_ <: J], oldValue: J, newValue: J): Unit = {
          v.update(p.value)
        }
      })
      v
    }
  }

  implicit class ReadOnlyPropertyExtensionsJ[T](p: javafx.beans.property.ReadOnlyProperty[T]) {
    def rx(): Rx[T] = {
      val v = Var(p.getValue)
      p.addListener(new ChangeListener[T] {
        override def changed(observable: ObservableValue[_ <: T], oldValue: T, newValue: T): Unit = {
          v.update(p.getValue)
        }
      })
      v
    }
  }

}
