package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}

import scala.util.Random

class Game(lvl: Level, plr: Player, world: World) extends Scene {
  var keysDown: List[Int] = List.empty
  var showingCharacterSheet = false
  var level: Level = lvl
  var descending = false
  var player: Player = plr
  var enemyTurn = false
  var floor = 1
  var cameraLocation: Vec2 = Vec2(0, 0)
  var updatingCameraX = false
  var updatingCameraY = false
  var allSpawned = false
  var clicked = false
  var mouseLocOnGrid: Vec2 = Vec2(0, 0)
  var enemies: List[Enemy] = List.empty
  override def init(): InputAdapter = {
    player.game = this
    player.archetype.onSelect(this)
    player.stats.health = player.stats.maxHealth
    new GameControl(this)
  }

  def updateCamera(): Unit = {
    if (updatingCameraX) {
      if (
        cameraLocation.x < (Geometry.ScreenWidth / 2 / screenUnit).toInt - player.location.x
      ) cameraLocation.x += 1
      else if (
        cameraLocation.x > (Geometry.ScreenWidth / 2 / screenUnit).toInt - player.location.x
      ) cameraLocation.x -= 1
      else updatingCameraX = false
    }
    if (updatingCameraY) {
      if (
        cameraLocation.y < (Geometry.ScreenHeight / 2 / screenUnit).toInt - player.location.y
      ) cameraLocation.y += 1
      else if (
        cameraLocation.y > (Geometry.ScreenHeight / 2 / screenUnit).toInt - player.location.y
      ) cameraLocation.y -= 1
      else updatingCameraY = false
    }
  }
  override def update(delta: Float): Option[Scene] = {
    if (!allSpawned) {
      for (i <- 0 until 10) {
        var loc = level.walkables.filterNot(w =>
          player.location == w && enemies.exists(e => e.location == w)
        )(
          Random.nextInt(
            level.walkables
              .filterNot(w =>
                player.location == w && enemies.exists(e => e.location == w)
              )
              .length
          )
        )
        var enemy = IceImp()
        enemy.location = loc
        enemy.game = this
        enemies = enemy :: enemies
      }
      allSpawned = true
    }
    if (
      player.location.x < -cameraLocation.x + 5 || player.location.x > -cameraLocation.x + (Geometry.ScreenWidth / screenUnit) - 5
    ) updatingCameraX = true
    if (
      player.location.y < -cameraLocation.y + 5 || player.location.y > -cameraLocation.y + (Geometry.ScreenHeight / screenUnit) - 5
    ) updatingCameraY = true
    if (updatingCameraX || updatingCameraY) updateCamera()
    player.update(delta)
    enemies.foreach(e => e.update(delta))
    enemyTurn = false
    if (descending) floor += 1
    if (descending) Some(new LevelGen(player, Some(this), world))
    else if (player.dead) Some(new GameOver(world))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    Trog.translationX = cameraLocation.x
    Trog.translationY = cameraLocation.y
    level.draw(batch)
    player.draw(batch)
    enemies.foreach(e => e.draw(batch))
    for (
      x <-
        -cameraLocation.x - 5 to (-cameraLocation.x + Geometry.ScreenWidth / screenUnit).toInt + 5
    ) {
      for (
        y <-
          -cameraLocation.y - 5 to (-cameraLocation.y + Geometry.ScreenHeight / screenUnit).toInt + 5
      ) {
        var dist = Int.MaxValue

        if (
          Math
            .sqrt(
              ((x - player.location.x) * (x - player.location.x)) + ((y - player.location.y) * (y - player.location.y))
            )
            .toInt < player.stats.sightRad
        ) {
          var path =
            Pathfinding.findPathUpto(player.location, Vec2(x, y), level)
          if (path.nonEmpty) {
            path.foreach(p => {
              dist = p.list.length
            })
          }
        }
        if (dist > player.stats.sightRad) {
          batch.setColor(0, 0, 0, 1)
        } else {
          var lightLevel: Float =
            ((((player.stats.sightRad - dist).toFloat / player.stats.sightRad) + .25f) min 1) max 0
          batch.setColor(
            0,
            0,
            0,
            1 - lightLevel
          )
        }
        batch.draw(
          Trog.Square,
          x * screenUnit,
          y * screenUnit,
          screenUnit,
          screenUnit
        )
      }
    }
  }

  def renderUI(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s"Lvl ${player.stats.level} ${player.archetype.name} on floor ${floor}",
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight
    )
    batch.setColor(Color.YELLOW)
    batch.draw(
      Trog.Square,
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4,
      screenUnit / 8
    )
    batch.setColor(Color.ORANGE)
    batch.draw(
      Trog.Square,
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4 * player.stats.exp / player.stats.nextExp,
      screenUnit / 8
    )
    batch.setColor(Color.FIREBRICK)
    batch.draw(
      Trog.Square,
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4,
      screenUnit / 2
    )
    batch.setColor(Color.RED)
    batch.draw(
      Trog.Square,
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4 * player.stats.health / player.stats.maxHealth,
      screenUnit / 2
    )
    batch.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s"${player.stats.health}/${player.stats.maxHealth}",
      -cameraLocation.x * screenUnit,
      -cameraLocation.y * screenUnit + Geometry.ScreenHeight - screenUnit
    )
    if (showingCharacterSheet) {
      batch.setColor(Color.DARK_GRAY)
      batch.draw(
        Trog.Square,
        -cameraLocation.x * screenUnit + (2 * screenUnit),
        -cameraLocation.y * screenUnit + (2 * screenUnit),
        (Geometry.ScreenWidth - (4 * screenUnit)),
        (Geometry.ScreenHeight - (4 * screenUnit))
      )
      Text.mediumFont.setColor(Color.WHITE)
      Text.mediumFont.draw(
        batch,
        s" \n " +
          s"Archetype: ${player.archetype.name}\n " +
          s"Level: ${player.stats.level}\n " +
          s"Experience: ${player.stats.exp}/${player.stats.nextExp}\n " +
          s"Floor: $floor\n " +
          s"Health: ${player.stats.health}/${player.stats.maxHealth}\n " +
          s"Armor Class: ${player.stats.ac}\n " +
          s"Sight Radius: ${player.stats.sightRad}\n " +
          s"Attack Bonus: ${player.stats.attackMod}\n " +
          s"Damage Bonus: ${player.stats.damageMod}\n " +
          s"Crit Modifier: %${player.stats.critMod*100}\n " +
          s"Crit Chance: %${player.stats.critChance}",
        -cameraLocation.x * screenUnit + (2 * screenUnit),
        (-cameraLocation.y * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
      )
    }
  }
}
class GameControl(game: Game) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    game.keysDown = keycode :: game.keysDown
    true
  }
  override def keyUp(keycode: Int): Boolean = {
    game.keysDown = game.keysDown.filterNot(f => f == keycode)
    if (keycode == Keys.C)
      game.showingCharacterSheet = !game.showingCharacterSheet
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    game.mouseLocOnGrid.x =
      (screenX / screenUnit).floor.toInt - game.cameraLocation.x
    game.mouseLocOnGrid.y =
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt - game.cameraLocation.y
    true
  }

  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    game.clicked = true
    true
  }

  override def touchUp(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    game.clicked = false
    true
  }
}
