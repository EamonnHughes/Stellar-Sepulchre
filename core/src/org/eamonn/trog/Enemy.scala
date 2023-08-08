package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.{Square, garbage}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

trait Enemy extends Actor {
  def initialize(gm: Game, loc: Vec2): Unit
  var game: Game
  var location: Vec2 = Vec2(0, 0)
  var dest: Vec2 = Vec2(0, 0)
  def texture: TextureWrapper
  def update(delta: Float): Unit
  def attack(target: Actor): Unit
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.RED)
    batch.draw(
      Square,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit * stats.health / stats.maxHealth,
      screenUnit * .1f
    )
    batch.setColor(Color.WHITE)
    batch.draw(
      texture,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(
      batch,
      stats.level.toString,
      location.x * screenUnit,
      (location.y + 1) * screenUnit
    )
  }
}

case class Criminal() extends Enemy {
  var equipment: Equipment = new Equipment
  var game: Game = _
  var lev: Int = _
  var name: String = _
  var stats: Stats = _
  def initialize(gm: Game, loc: Vec2): Unit = {
    game = gm
    location = loc
    lev = Random.nextInt(game.floor) + 1 + (Random.nextInt(11) / 10)
    var weapon: Weapon = makeCommonItem(0, game, 1, 3)
    if (lev > 4) {
      weapon = makeCommonItem(0, game, 1, 6)
    }
    weapon.possessor = Some(this)
    weapon.location = None
    game.items = weapon :: game.items
    equipment.weapon = Some(weapon)
    name = enemyNames
      .getCriminalName(lev)(
        Random.nextInt(enemyNames.getCriminalName(lev).length)
      )
      .substring(1)
    stats = makeStats(
      mAc = (3 + lev) min 9,
      mExp = 5 * lev,
      mNExp = 0,
      mMHeal = 5 * lev,
      mHeal = 5 * lev,
      mSrad = 7,
      mLev = lev,
      mDmg = 0,
      mAtk = .35f * lev,
      mCrc = 3 + (lev min 10),
      mCrm = 1.9f + (lev * .1f)
    )
    game.enemies = this :: game.enemies
  }
  def texture: TextureWrapper = TextureWrapper.load(s"Criminal${lev min 5}.png")

  override def update(delta: Float): Unit = {

    if (game.enemyTurn) {
      dest = game.player.location.copy()
      var path = Pathfinding
        .findPath(location, dest, game.level)
        .filter(p => p.list.length < stats.sightRad)
      path.foreach(p => {
        var next = p.list.reverse(1).copy()
        if (!game.enemies.exists(e => e.location == next)) {
          if (game.player.location == next) attack(game.player)
          else location = next.copy()
        }
      })
    }

    if (stats.health <= 0) {
      equipment.weapon.foreach(w => {
        w.possessor = None
        w.location = Some(location.copy())
      })
      game.enemies = game.enemies.filterNot(e => e eq this)
      game.player.stats.exp += stats.exp
      game.addMessage(name + " has been slain")
    }
  }

  override def attack(target: Actor): Unit = {
    if (equipment.weapon.nonEmpty) {
      equipment.weapon.foreach(w => w.onAttack(this, target))
    } else if (d(10) > target.stats.ac) {
      target.stats.health -= 1
      if (target == game.player) game.player.lastStrike = s"a $name"
    }
  }

}

object enemyNames {
  val criminalNames: List[String] = List(
    "1Cutpurse",
    "1Thief",
    "2Mugger",
    "2Bandit",
    "3Bandit Captain",
    "3Smuggler",
    "4Arms Dealer",
    "4Thieves Guild Officer",
    "5Thief Lord"
  )
  def getCriminalName(lev: Int): List[String] = {
    criminalNames.filter(c => c.charAt(0) == (lev min 5).toString.charAt(0))
  }
}
