package org.eamonn.trog.character

import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Actor, Pathfinding, d}

trait Skill {
  val coolDown: Int
  val takesTurn: Boolean
  var name: String
  var ccd: Int

  def icon: TextureWrapper
}

trait rangedSkill extends Skill {
  var range: Int

  def onUse(
             user: Actor,
             target: Actor,
             game: Game
           ): Unit
}

trait meleeSkill extends Skill {
  def onUse(
             user: Actor,
             target: Actor,
             game: Game
           ): Unit

  def selectTarget(game: Game, user: Actor): Option[Actor] = {
    game.enemies.find(e => user.location.getAdjacents.contains(e.location))
  }
}

case class throwDagger() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Throw Dagger"
  override var ccd: Int = 0
  override var range: Int = 5

  override def icon: TextureWrapper = TextureWrapper.load("ThrowDagger.png")

  override def onUse(
                      user: Actor,
                      target: Actor,
                      game: Game
                    ): Unit = {
    var ended = false
    Pathfinding
      .findPath(user.location, target.location, game.level)
      .filter(p => p.list.length <= range)
      .foreach(p => {
        game.enemies.foreach(e => {
          if (p.list.contains(e.location) && !ended) {
            e.stats.health -= d(3)
            ended = true
          }
        })
      })

  }
}

case class Dash() extends rangedSkill {
  override val coolDown: Int = 5
  override val takesTurn: Boolean = true
  override var name: String = "Dash"
  override var ccd: Int = 0
  override var range: Int = 4

  override def icon: TextureWrapper = TextureWrapper.load("Dash.png")

  override def onUse(
                      user: Actor,
                      target: Actor,
                      game: Game
                    ): Unit = {
    Pathfinding
      .findPath(user.location, target.location, game.level)
      .filter(p => p.list.length <= range)
      .foreach(p => {
        if (!game.enemies.exists(e => e.location == p.list(1))) {
          user.location = p.list(1).copy()
          user.destination = p.list(1).copy()
          user.attack(target)
        }
      })

  }
}

case class shieldBash() extends meleeSkill {
  override val coolDown: Int = 4
  override val takesTurn: Boolean = true
  override var name: String = "Shield Bash"
  override var ccd: Int = 0

  override def icon: TextureWrapper = TextureWrapper.load("ShieldBash.png")

  override def onUse(
                      user: Actor,
                      target: Actor,
                      game: Game
                    ): Unit = {
    user.attack(target)
    target.statuses.stunned = 2

  }
}
