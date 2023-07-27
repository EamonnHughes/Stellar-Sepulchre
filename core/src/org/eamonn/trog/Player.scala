package org.eamonn.trog

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

case class Player() extends Actor {
  var ac = 5
  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }
  var exp = 0
  var resting = false
  var nextExp = 50
  var maxHealth = 10
  var health: Int = maxHealth
  var sightRad = 6
  var healing = 0
  var level = 1
  var inCombat = false
  var damageMod = 0
  var attackMod = 0
  var playerIcon: TextureWrapper = TextureWrapper.load("charv1.png")
  var game: Game = _
  var ready = false
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  var speed = .25f
  def attack(target: Enemy): Unit = {
    if (d(10) + attackMod > target.ac) {
      target.health -= (d(3) + damageMod)
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
    println(inCombat)
    if (resting) speed = .05f else speed = .25f
    if (healing > 4 && health < maxHealth) {
      health += 1
      healing = 0
    }
    if (exp >= nextExp) {
      exp -= nextExp
      nextExp *= 2
      maxHealth += 10
      health = maxHealth
      damageMod += 1
      attackMod += 1
      ac += 1
      level += 1
    }
    if (
      game.enemies.exists(e => {
        var path = Pathfinding.findPath(e.location, location, game.level)
        var dist = Int.MaxValue
        path.foreach(p => {
          dist = p.list.length
        })
        dist < sightRad
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
      if (game.keysDown.contains(Keys.S)) {
        destination.y = location.y - 1
        destination.x = location.x
      } else if (game.keysDown.contains(Keys.W)) {
        destination.y = location.y + 1
        destination.x = location.x
      } else if (game.keysDown.contains(Keys.D)) {
        destination.y = location.y
        destination.x = location.x + 1
      } else if (game.keysDown.contains(Keys.A)) {
        destination.y = location.y
        destination.x = location.x - 1
      } else if (game.keysDown.contains(Keys.SPACE)) {
        yourTurn = false
        if (!inCombat && health < maxHealth) healing += 1
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
    if (resting && (health == maxHealth || inCombat)) {
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
        if (!inCombat && health < maxHealth) healing += 1
      }
      if (!inCombat && health < maxHealth) healing += 1
      yourTurn = false
      game.enemyTurn = true
    }
  }
}
