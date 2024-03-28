package org.eamonnh.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.{Square, asleep}
import org.eamonnh.trog.character.{Equipment, Stats, makeStats}
import org.eamonnh.trog.items.{MedKit, Weapon, makeCommonWeapon}
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.Animation

import scala.util.Random

trait Enemy extends Actor {
  var game: Game
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var texture: String

  def initialize(gm: Game, loc: Vec2): Unit

  def update(delta: Float): Unit

  def attack(target: Actor): Unit

  def draw(batch: PolygonSpriteBatch): Unit = {
    if (statuses.stunned > 0) {
      batch.setColor(Color.YELLOW)
      batch.draw(
        asleep,
        location.x * screenUnit,
        location.y * screenUnit,
        screenUnit,
        screenUnit
      )
    }
    if (stats.health > 0) {
      batch.setColor(Color.RED)
      batch.draw(
        Square,
        location.x * screenUnit,
        location.y * screenUnit,
        screenUnit * stats.health / stats.maxHealth,
        screenUnit * .05f
      )
    }
    batch.setColor(Color.WHITE)
    Animation.twoFrameAnimation(
      game,
      batch,
      texture,
      location.x.toFloat,
      location.y.toFloat
    )

    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(
      batch,
      stats.level.toString,
      location.x * screenUnit,
      (location.y + 1) * screenUnit
    )

    if (selected) {
      batch.setColor(1f, 1f, 0, .75f)
      Animation.twoFrameAnimation(
        game,
        batch,
        "targetReticle",
        location.x.toFloat,
        location.y.toFloat
      )
    }
  }
}

case class Servitor() extends Enemy {
  var equipment: Equipment = new Equipment
  var game: Game = _
  var lev: Int = _
  var name: String = _
  var stats: Stats = _
  var texture: String = s"Servitor${{
    lev min 5
  } + 1}_"

  def initialize(gm: Game, loc: Vec2): Unit = {
    game = gm
    location = loc
    lev = Random.nextInt(game.floor) + 1 + (Random.nextInt(11) / 10)
    var weapon: Weapon = makeCommonWeapon(0, game, 1, (lev min 5) + 1, "Drone")
    weapon.possessor = Some(this)
    weapon.location = None
    game.items = weapon :: game.items
    equipment.weapon = Some(weapon)
    name = enemyNames
      .getServitorName(lev)(
        Random.nextInt(enemyNames.getServitorName(lev).length)
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

  override def update(delta: Float): Unit = {

    if (game.enemyTurn) {
      if (statuses.stunned > 0) statuses.stunned -= 1
      else {
        destination = game.player.location.copy()
        var path = Pathfinding
          .findPath(location, destination, game.level)
          .filter(p => p.list.length < stats.sightRad)
        path.foreach(p => {
          var next = p.list.reverse(1).copy()
          if (!game.enemies.exists(e => e.location == next)) {
            if (game.player.location == next) attack(game.player)
            else location = next.copy()
          }
        })
      }
    }

    if (stats.health <= 0) {
      var w = new MedKit()
      w.location = Some(location.copy())
      w.game = game
      game.items = w :: game.items
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
  val Servo: List[String] = List(
    "1Drone",
    "1Menial",
    "2Servitor",
    "3Suppressor",
    "4Hemisynapt",
    "5Synapt"
  )

  def getServitorName(lev: Int): List[String] = {
    Servo.filter(c => c.charAt(0) == (lev min 5).toString.charAt(0))
  }
}
