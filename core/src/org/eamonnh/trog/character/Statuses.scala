package org.eamonnh.trog.character

import org.eamonnh.trog.Actor

trait Status {
  def onTick(reciever: Actor): Unit

  var timeLeft: Int
}

case class Stunned() extends Status {

  override def onTick(reciever: Actor): Unit = {
    reciever.turn = false
  }

  override var timeLeft: Int = _
}


case class Bleeding() extends Status {

  override def onTick(reciever: Actor): Unit = {
    reciever.stats.health -= 1
  }

  override var timeLeft: Int = _
}
