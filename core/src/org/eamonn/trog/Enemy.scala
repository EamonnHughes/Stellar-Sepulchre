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
  def texture: TextureWrapper
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

case class Humanoid(gm: Game) extends Enemy {
  var equipment: Equipment = new Equipment
  var name: String = "Bandit"
  equipment.weapon = Some(Dagger(0))
  def texture: TextureWrapper = Trog.humanoidHostileTexture
  var game: Game = gm
  var lev: Int = Random.nextInt(game.floor) + 1 + (Random.nextInt(11)/10)
  var stats: Stats = makeStats(
    mAc = (3 + lev) min 9,
    mExp = 5*lev,
    mNExp = 0,
    mMHeal = 5*lev,
    mHeal = 5*lev,
    mSrad = 7,
    mLev = lev,
    mDmg = 0,
    mAtk = .35f*lev,
    mCrc = 3+(lev min 10),
    mCrm = 1.9f+(lev*.1f)
  )

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
      game.addMessage(name + " has been slain")
    }
  }

  override def attack(target: Actor): Unit = {
    if(equipment.weapon.nonEmpty){

      equipment.weapon.foreach(w => w.onAttack(this, target))
    } else if(d(10) > target.stats.ac) target.stats.health -= 1
  }

}
