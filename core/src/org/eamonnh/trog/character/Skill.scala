package org.eamonnh.trog.character

import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.TextureWrapper
import org.eamonnh.trog.{Actor, Pathfinding, Trog, Vec2, d}

trait Skill {
  val coolDown: Int
  val takesTurn: Boolean
  var name: String
  var ccd: Int

  def icon: TextureWrapper
}

trait rangedSkill extends Skill {
  var maxRange: Int
  var minRange: Int

  def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit
}

case class MicroMissile() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Micromissile"
  override var ccd: Int = 0
  override var maxRange: Int = 5

  override def icon: TextureWrapper = TextureWrapper.load("Micromissile.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    var ended = false
    Pathfinding
      .findPath(user.location, target, game.level)
      .filter(p => p.list.length <= maxRange)
      .foreach(p => {
        Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
        game.enemies.foreach(e => {
          if (p.list.contains(e.location) && !ended) {
            e.stats.health -= d(3)
            ended = true
          }
        })
      })

  }

  override var minRange: Int = 2
}

case class Charge() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Charge"
  override var ccd: Int = 0
  override var maxRange: Int = 5

  override def icon: TextureWrapper = TextureWrapper.load("Charge.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    Pathfinding
      .findPath(user.location, target, game.level)
      .filter(p => p.list.length <= maxRange)
      .foreach(p => {
        if (!game.enemies.exists(e => e.location == p.list(1))) {
          Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
          user.location = p.list(1).copy()
          user.destination = p.list(1).copy()
          game.enemies.filter(e => e.location == target).foreach(t => user.attack(t))
        }
      })

  }

  override var minRange: Int = 3
}

case class Bash() extends rangedSkill {
  override val coolDown: Int = 4
  override val takesTurn: Boolean = true
  override var name: String = "Bash"
  override var ccd: Int = 0
  override var maxRange: Int = 2
  override var minRange: Int = 2


  override def icon: TextureWrapper = TextureWrapper.load("Bash.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    game.enemies.filter(e => e.location == target).foreach(enemy => {
    user.attack(enemy)
    enemy.statuses.stunned = 4
    Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
  })
  }
}
