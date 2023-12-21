package org.eamonn.trog.items

import org.eamonn.trog
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Actor, d}

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
