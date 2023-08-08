package org.eamonn.trog.items

import org.eamonn.trog
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Actor, d}

trait Potion extends Usable {}

case class HealingPotion() extends Potion {
  override def use(user: Actor): Unit = {
    if (user.stats.health < user.stats.maxHealth && number >= 1) {
      user.stats.health = (user.stats.health + d(6)) min user.stats.maxHealth
      number -= 1
      game.addMessage("You consumed 1 " + name)
    }
  }

  override var game: Game = _

  override def name: String = "Healing Potion"

  override def groundTexture: TextureWrapper =
    TextureWrapper.load("HealingPotion.png")

  override var location: Option[trog.Vec2] = None

  override var possessor: Option[Actor] = None

  override def tNum: Int = number
}
