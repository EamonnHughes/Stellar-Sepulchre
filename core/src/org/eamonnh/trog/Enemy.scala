package org.eamonnh.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.{Square, asleep}
import org.eamonnh.trog.character.{Equipment, Stats, Stunned, makeStats}
import org.eamonnh.trog.items.{DroneGiver, Item, MedKit, NoItem, Weapon, makeCommonWeapon}
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.Animation

import scala.util.Random

trait Enemy extends Actor {
  var turn = false
  var game: Game
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var texture: String

  def initialize(gm: Game, loc: Vec2): Unit

  def update(delta: Float): Unit

  def attack(target: Actor): Unit

  def draw(batch: PolygonSpriteBatch): Unit = {
    if (statuses.exists(_.isInstanceOf[Stunned])) {
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
    /*
    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(
      batch,
      stats.level.toString,
      location.x * screenUnit,
      (location.y + 1) * screenUnit
    )
     */
  }
  def Die(): Unit = {
    var item: Item = NoItem()
    if(Math.random() > .75){
      item = MedKit()
    } else if(Math.random() > .66 && equipment.weapon.nonEmpty){
      item = makeCommonWeapon(0, game, equipment.weapon.head.numOfDice, equipment.weapon.head.diceVal, DroneGiver())
    }
    if(!item.isInstanceOf[NoItem]) {
      item.location = Some(location.copy())
      item.game = game
      game.items = item :: game.items
    }
    game.enemies = game.enemies.filterNot(e => e eq this)
    game.player.stats.exp += stats.exp
    game.addMessage(name + " has been slain")
  }
}

trait BasicMeleeEnemy extends Enemy {
  override def update(delta: Float): Unit = {

    if (turn) {
      statuses.foreach(s => {
        s.onTick(this)
        s.timeLeft -= 1
        if(s.timeLeft == 0) statuses = statuses.filterNot( s2 => s2 eq s)
      })}
    if(turn) {
      destination = game.player.location.copy()
    var path = Pathfinding
        .findPathWithEnemies(location, destination, game.level, game.enemies.filterNot(e => e eq this))
        .filter(p => p.list.length < stats.sightRad)
      path.foreach(p => {
        var next = p.list.reverse(1).copy()
        if (!game.enemies.exists(e => e.location == next)) {
          if (game.player.location == next) attack(game.player)
          else location = next.copy()
        }
      })
      turn = false
    }
  }
}

case class Servitor() extends BasicMeleeEnemy {
  var equipment: Equipment = new Equipment
  var game: Game = _
  var lev: Int = _
  var name: String = _
  var stats: Stats = _
  var texture: String = _

  def initialize(gm: Game, loc: Vec2): Unit = {
    game = gm
    location = loc
    lev = Random.nextInt(game.floor) + 1 + (Random.nextInt(11) / 10)
    var weapon: Weapon = makeCommonWeapon(0, game, 1, (lev min 5) + 1, DroneGiver())
    weapon.possessor = Some(this)
    weapon.location = None
    game.items = weapon :: game.items
    equipment.weapon = Some(weapon)
    name = enemyNames
      .getServitorName(lev)(
        Random.nextInt(enemyNames.getServitorName(lev).length)
      )
      .substring(1)
    texture = s"Servitor${{
      lev min 5
    }}_"
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
    "2Servitor",
    "3Suppressor",
    "4Hemisynapt",
    "5Synapt"
  )

  def getServitorName(lev: Int): List[String] = {
    Servo.filter(c => c.charAt(0) == (lev min 5).toString.charAt(0))
  }
}
