package org.eamonn.trog

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.character.{Archetype, Archetypes}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

case class Player() extends Actor {
  var archetype: Archetype = _
  var initialized = false
  var healing = 0f
  var healingFactor = 0.1f
  var equipment: Equipment = new Equipment
  var resting = false
  var name = ""
  var dead = false
  var stats: Stats = basePlayerStats()
  var inCombat = false
  var game: Game = _
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  var speed = .25f
  def initially(gme: Game): Unit = {
    val weapon = Sword(0)
    gme.items = weapon :: gme.items
    equipment.weapon = Some(weapon)
    archetype.onSelect(gme)
    game = gme
    stats.health = stats.maxHealth
    initialized = true
  }
  def playerIcon: TextureWrapper = Trog.playerTexture
  def levelUp(): Unit = {
    stats.exp -= stats.nextExp
    stats.nextExp *= 2
    stats.maxHealth += d(2, 5)
    archetype.onLevelUp(game)
    stats.health = stats.maxHealth
    stats.level += 1
  }
  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }
  def attack(target: Enemy): Unit = {
    if (equipment.weapon.nonEmpty) {
      equipment.weapon.foreach(w => w.onAttack(this, target))
    } else {
      if (d(10) > target.stats.ac) {
        target.stats.health -= 1
      }
    }
  }
  def draw(batch: PolygonSpriteBatch) = {
    batch.setColor(Color.WHITE)
    batch.draw(
      playerIcon,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  def update(delta: Float) = {
    if (stats.health <= 0) dead = true
    if (resting) speed = .005f else speed = .25f
    if (healing > 4 && stats.health < stats.maxHealth) {
      stats.health += 1
      healing = 0
    }
    if (stats.exp >= stats.nextExp) {
      levelUp()
    }
    if (
      game.enemies.exists(e => {
        val path = Pathfinding.findPath(e.location, location, game.level)
        var dist = Int.MaxValue
        path.foreach(p => {
          dist = p.list.length
        })
        dist < stats.sightRad
      })
    ) inCombat = true
    else inCombat = false
    if (!yourTurn) {
      tick += delta
      if (tick > speed) {
        yourTurn = true
        tick = 0f
      }
    }
    if (yourTurn) {
      if (game.keysDown.contains(Keys.S) || game.keysDown.contains(Keys.DOWN)) {
        destination.y = location.y - 1
        destination.x = location.x
      } else if (
        game.keysDown.contains(Keys.W) || game.keysDown.contains(Keys.UP)
      ) {
        destination.y = location.y + 1
        destination.x = location.x
      } else if (
        game.keysDown.contains(Keys.D) || game.keysDown.contains(Keys.RIGHT)
      ) {
        destination.y = location.y
        destination.x = location.x + 1
      } else if (
        game.keysDown.contains(Keys.A) || game.keysDown.contains(Keys.LEFT)
      ) {
        destination.y = location.y
        destination.x = location.x - 1
      } else if (game.keysDown.contains(Keys.SPACE)) {
        resting = true
        game.enemyTurn = true
      } else if (game.clicked) {
        destination = game.mouseLocOnGrid.copy()
      } else if (game.keysDown.contains(Keys.R)) {
        resting = true
      } else if (
        game.keysDown.contains(Keys.PERIOD) && (game.keysDown.contains(
          Keys.SHIFT_RIGHT
        ) || game.keysDown.contains(Keys.SHIFT_LEFT))
      ) {
        tryToGoDown()
      }
    }
    if ((destination != location || resting) && yourTurn) {
      if (!resting) {
        val path = Pathfinding.findPath(location, destination, game.level)
        path.foreach(p => {
          val dest = p.list.reverse(1).copy()
          val enemy = game.enemies.filter(e => e.location == dest)
          if (enemy.isEmpty) { location = dest.copy() }
          else {
            attack(enemy.head)
            destination = location.copy()
          }
        })
      } else {
        destination = location.copy()
        if (!inCombat && stats.health < stats.maxHealth) (healing += healingFactor*10)
      }
      if (stats.health < stats.maxHealth) healing += healingFactor
      yourTurn = false
      game.enemyTurn = true
    }
    if (resting && (stats.health == stats.maxHealth || inCombat)) {
      resting = false
    }
  }
}
