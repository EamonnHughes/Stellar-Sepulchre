package org.eamonnh.trog.items

import org.eamonnh.trog
import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.TextureWrapper
import org.eamonnh.trog.{Actor, d}

trait Consumable extends Usable {}

case class MedKit() extends Consumable {
  override var game: Game = _
  override var location: Option[trog.Vec2] = None

  override def use(user: Actor): Unit = {
    if (user.stats.health < user.stats.maxHealth && number >= 1) {
      user.stats.health = (user.stats.health + d(2, 4)) min user.stats.maxHealth
      number -= 1
      game.addMessage("You consumed 1 " + name)
    }
  }

  override def name: String = "MedKit"

  override def groundTexture: TextureWrapper =
    TextureWrapper.load("MedKit.png")

  override def tNum: Int = number
}
