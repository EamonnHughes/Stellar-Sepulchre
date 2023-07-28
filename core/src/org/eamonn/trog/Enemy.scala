package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.{Square, garbage}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

trait Enemy extends Actor {
  override var stats: Stats
  var game: Game
  var location: Vec2 = Vec2(0, 0)
  var dest: Vec2 = Vec2(0, 0)
  var texture: TextureWrapper
  def update(delta: Float): Unit
  def attack(target: Actor): Unit
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.RED)
    batch.draw(Square, location.x * screenUnit, location.y * screenUnit, screenUnit * stats.health/stats.maxHealth, screenUnit*.1f)
    batch.setColor(Color.WHITE)
    batch.draw(texture, location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(batch, stats.level.toString, location.x * screenUnit, (location.y + 1) * screenUnit)
  }
}

case class IceImp(gm: Game) extends Enemy {
  var game: Game = gm
  var texture: TextureWrapper = TextureWrapper.load("iceimp.png")
  var stats: Stats = Stats()
  stats.level = Random.nextInt(game.floor) + 1
  stats.maxHealth = 5*stats.level
  stats.health = 5*stats.level
  stats.ac = 3+stats.level
  stats.sightRad = 10
  stats.exp = 10*stats.level

  override def update(delta: Float): Unit = {


      if (game.enemyTurn) {
        dest = game.player.location.copy()
        var path = Pathfinding.findPath(location, dest, game.level).filter(p =>p.list.length < stats.sightRad)
        path.foreach(p => {
          var next = p.list.reverse(1).copy()
          if(!game.enemies.exists(e => e.location == next)) {
            if (game.player.location == next) attack(game.player) else location = next.copy()
          }
        })
      }

    if(stats.health <= 0){
      game.enemies = game.enemies.filterNot(e => e eq this)
      game.player.stats.exp += stats.exp
    }
  }

  override def attack(target: Actor): Unit = {
   if(d(10) > target.stats.ac) target.stats.health -= d(2)
  }
}
