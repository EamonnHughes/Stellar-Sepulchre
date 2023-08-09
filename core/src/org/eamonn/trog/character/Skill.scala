package org.eamonn.trog.character

import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.{Actor, Pathfinding, Vec2, d}
import org.eamonn.trog.util.TextureWrapper
import cats.syntax.option._

trait Skill {
  var name: String
  def icon: TextureWrapper
  val coolDown: Int
  var ccd: Int
  val takesTurn: Boolean
}

trait rangedSkill extends Skill {
  var range: Int
  def onUse(
      user: Actor,
      target: Actor,
      game: Game
  ): Unit
  def selectTarget(game: Game, user: Actor): Option[Actor] = {
    game.enemies.minByOption(e => {
      var dist = Int.MaxValue
      Pathfinding
        .findPath(user.location, e.location, game.level)
        .foreach(p => {
          dist = p.list.length
        })
      dist
    })
  }
}

case class throwDagger() extends rangedSkill {
  override var name: String = "Throw Dagger"

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

  override val coolDown: Int = 5
  override var ccd: Int = 0
  override val takesTurn: Boolean = true
  override var range: Int = 5
}
