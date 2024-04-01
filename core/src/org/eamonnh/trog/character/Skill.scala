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
  var technicalMaxRange: Int
  def maxRange(user: Actor): Int = technicalMaxRange min user.stats.sightRad
  var minRange: Int
  var mustTargetEnemy: Boolean
  var canTargetEnemy: Boolean

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
  override var technicalMaxRange: Int = 5
  override var minRange: Int = 2
  override var mustTargetEnemy: Boolean = true
  override var canTargetEnemy: Boolean = true

  override def icon: TextureWrapper = TextureWrapper.load("Micromissile.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    var ended = false
    Pathfinding
      .findPath(user.location, target, game.level)
      .filter(p => p.list.length <= maxRange(user))
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
}

case class Charge() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Charge"
  override var ccd: Int = 0
  override var technicalMaxRange: Int = 5
  override var minRange: Int = 3
  override var mustTargetEnemy: Boolean = true
  override var canTargetEnemy: Boolean = true

  override def icon: TextureWrapper = TextureWrapper.load("Charge.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    Pathfinding
      .findPath(user.location, target, game.level)
      .filter(p => p.list.length <= maxRange(user))
      .foreach(p => {
        if (!game.enemies.exists(e => e.location == p.list(1))) {
          Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
          user.location = p.list(1).copy()
          user.destination = p.list(1).copy()
          game.enemies
            .filter(e => e.location == target)
            .foreach(t => user.attack(t))
        }
      })

  }
}

case class Bash() extends rangedSkill {
  override val coolDown: Int = 4
  override val takesTurn: Boolean = true
  override var name: String = "Bash"
  override var ccd: Int = 0
  override var technicalMaxRange: Int = 2
  override var minRange: Int = 2
  override var mustTargetEnemy: Boolean = true
  override var canTargetEnemy: Boolean = true

  override def icon: TextureWrapper = TextureWrapper.load("Bash.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    game.enemies
      .filter(e => e.location == target)
      .foreach(enemy => {
        user.attack(enemy)
        var stun = Stunned()
        stun.timeLeft = 4
        enemy.statuses = stun :: enemy.statuses
        Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
      })
  }
}

case class Disengage() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Disengage"
  override var ccd: Int = 0
  override var technicalMaxRange: Int = 5
  override var minRange: Int = 2
  override var mustTargetEnemy: Boolean = false
  override var canTargetEnemy: Boolean = false

  override def icon: TextureWrapper = TextureWrapper.load("Disengage.png")

  override def onUse(
      user: Actor,
      target: Vec2,
      game: Game
  ): Unit = {
    user.location = target.copy()

  }
}
