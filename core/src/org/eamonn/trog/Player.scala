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
  var archApplied = false
  def levelUp(): Unit = {
    stats.exp -=stats.nextExp
    stats.nextExp *= 2
    stats.maxHealth += d(2, 5)
    archetype.onLevelUp(game)
    stats.health = stats.maxHealth
    stats.level += 1
  }
  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }
  var resting = false
  var dead = false
  var stats = Stats()
  var inCombat = false
  def playerIcon: TextureWrapper = Trog.playerTexture
  var game: Game = _
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  var speed = .25f
  def attack(target: Enemy): Unit = {
    if (d(10) + stats.attackMod > target.stats.ac) {
      var damage = (d(3) + stats.damageMod)
      if(Random.nextInt(100) <= stats.critChance) damage = (stats.critMod*damage).toInt
      target.stats.health -= damage
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
    if(stats.health <= 0) dead = true
    if (resting) speed = .05f else speed = .25f
    if (stats.healing > 4 && stats.health < stats.maxHealth) {
      stats.health += 1
      stats.healing = 0
    }
    if (stats.exp >= stats.nextExp) {
      levelUp()
    }
    if (
      game.enemies.exists(e => {
        var path = Pathfinding.findPath(e.location, location, game.level)
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
      } else if (game.keysDown.contains(Keys.W) || game.keysDown.contains(Keys.UP)) {
        destination.y = location.y + 1
        destination.x = location.x
      } else if (game.keysDown.contains(Keys.D) || game.keysDown.contains(Keys.RIGHT)) {
        destination.y = location.y
        destination.x = location.x + 1
      } else if (game.keysDown.contains(Keys.A) || game.keysDown.contains(Keys.LEFT)) {
        destination.y = location.y
        destination.x = location.x - 1
      } else if (game.keysDown.contains(Keys.SPACE)) {
        yourTurn = false
        game.enemyTurn = true
        if (!inCombat && stats.health < stats.maxHealth) stats.healing += 1
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
    if (resting && (stats.health == stats.maxHealth || inCombat)) {
      resting = false
    }
    if ((destination != location || resting) && yourTurn) {
      if (!resting) {
        var path = Pathfinding.findPath(location, destination, game.level)
        path.foreach(p => {
          var dest = p.list.reverse(1).copy()
          var enemy = game.enemies.filter(e => e.location == dest)
          if (enemy.isEmpty) { location = dest.copy() }
          else {
            attack(enemy.head)
            destination = location.copy()
          }
        })
      } else {
        destination = location.copy()
        if (!inCombat && stats.health < stats.maxHealth) stats.healing += 1
      }
      if (!inCombat && stats.health < stats.maxHealth) stats.healing += 1
      yourTurn = false
      game.enemyTurn = true
    }
  }
}

