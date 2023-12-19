package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.SaveLoad.{loadState, saveState}
import org.eamonn.trog.Scene
import org.eamonn.trog.character.Player
import org.eamonn.trog.items.Item
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}

import scala.util.Random

class Game(lvl: Level, plr: Player, wld: World)
    extends Scene
    with Serializable {
  override def renderUI(batch: PolygonSpriteBatch): Unit = inGameUserInterface.renderUI(batch, this)
  def addMessage(message: String): Unit = {
    messages = message :: messages
  }
  var clickedForTargeting = false
  var fakeLoc = Vec2(0, 0)
  var saveTick = 0f
  var explored: List[Vec2] = List.empty
  var loadable = false
  var messages = List.empty[String]
  var world = wld
  var keysDown: List[Int] = List.empty
  var level: Level = lvl
  var descending = false
  var player: Player = plr
  var enemyTurn = false
  var floor = 1
  var updatingCameraX = false
  var updatingCameraY = false
  var mainMenuing = false
  var allSpawned = false
  var clicked = false
  var mouseLocOnGrid: Vec2 = Vec2(0, 0)
  var enemies: List[Enemy] = List.empty
  var items: List[Item] = List.empty

  var animateTime = 0f
  def home: Home = {
    var h = new Home(world)
    h.game = loadState(0)
    h
  }
  override def init(): InputAdapter = {
    Trog.inGameOST.loop(.4f)
    player.game = this
    if (!player.initialized) {
      player.initially(this)
    }
    new GameControl(this)
  }

  def updateCamera(): Unit = {
    if (
      player.location.x < -Trog.translationX + 5 || player.location.x > -Trog.translationX + (Geometry.ScreenWidth / screenUnit) - 5
    ) updatingCameraX = true
    if (
      player.location.y < -Trog.translationY + 5 || player.location.y > -Trog.translationY + (Geometry.ScreenHeight / screenUnit) - 5
    ) updatingCameraY = true
    if (updatingCameraX || updatingCameraY) {
      if (updatingCameraX) {
        if (
          Trog.translationX < (Geometry.ScreenWidth / 2 / screenUnit).toInt - player.location.x
        ) Trog.translationX += 1
        else if (
          Trog.translationX > (Geometry.ScreenWidth / 2 / screenUnit).toInt - player.location.x
        ) Trog.translationX -= 1
        else updatingCameraX = false
      }
      if (updatingCameraY) {
        if (
          Trog.translationY < (Geometry.ScreenHeight / 2 / screenUnit).toInt - player.location.y
        ) Trog.translationY += 1
        else if (
          Trog.translationY > (Geometry.ScreenHeight / 2 / screenUnit).toInt - player.location.y
        ) Trog.translationY -= 1
        else updatingCameraY = false
      }
    }
  }
  override def update(delta: Float): Option[Scene] = {
    mouseLocOnGrid.x =
      (fakeLoc.x / screenUnit).floor.toInt - Trog.translationX
    mouseLocOnGrid.y =
      ((Geometry.ScreenHeight - fakeLoc.y) / screenUnit).floor.toInt - Trog.translationY
    animateTime = animateTime+delta
    while (animateTime >= 1) animateTime-=1
    enemies.foreach(e => e.selected = false)
    items.foreach(ite => {
      if (ite.number < 1) items = items.filterNot(item => item eq ite)
    })
    if (keysDown.contains(Keys.CONTROL_LEFT)) {
      if (keysDown.contains(Keys.S)) {
        saveState(this, 0)
      } else if (keysDown.contains(Keys.Q)) {
        saveState(this, 0)
        mainMenuing = true
      }
    }
    if (!allSpawned) {
      for (i <- 0 until (floor * 10).toInt) {
        var loc = level.walkables.filterNot(w =>
          player.location == w || enemies.exists(e => e.location == w)
        )(
          Random.nextInt(
            level.walkables
              .filterNot(w =>
                player.location == w || enemies.exists(e => e.location == w)
              )
              .length
          )
        )
        var enemy = Servitor()
        enemy.initialize(this, loc)
      }
      allSpawned = true
    }
    if (player.rangedSkillUsing.nonEmpty) {
      player.rangedSkillUsing.foreach(sk => {
        player.rangedSkillTargetables = enemies.filter(e => {
          Pathfinding
            .findPath(player.location, e.location, level)
            .exists(p => p.list.length <= sk.range)
        })
        if (player.rangedSkillTargetables.isEmpty) {
          player.clearRangedStuff()
        } else {
          player.rangedSkillTargetables(player.rangedSkillOption).selected =
            true
          if (keysDown.contains(Keys.LEFT)) {
            if (!clickedForTargeting) {
              player.rangedSkillOption =
                (player.rangedSkillOption + (player.rangedSkillTargetables.length - 1)) % player.rangedSkillTargetables.length
            }
            clickedForTargeting = true
          } else if (keysDown.contains(Keys.RIGHT)) {
            if (!clickedForTargeting) {
              player.rangedSkillOption =
                (player.rangedSkillOption + 1) % player.rangedSkillTargetables.length
            }
            clickedForTargeting = true
          } else {
            clickedForTargeting = false
          }
          if (keysDown.contains(Keys.ENTER)) {
            sk.onUse(
              player,
              player.rangedSkillTargetables(player.rangedSkillOption),
              this
            )
            sk.ccd = sk.coolDown
            if (sk.takesTurn) {
              player.yourTurn = false
              enemyTurn = true
            }
            player.clearRangedStuff()
          }
          if (keysDown.contains(Keys.ESCAPE)) {
            player.clearRangedStuff()
          }
        }
      })
    } else {
      player.update(delta)
      enemies.foreach(e => e.update(delta))
      if (enemyTurn) {
        saveTick += 1
        enemyTurn = false
        player.stats.skills.foreach(sk => if (sk.ccd > 0) sk.ccd -= 1)
      }
      if (saveTick >= 25) {
        var kd = keysDown
        saveState(this, 0)
        keysDown = kd
        saveTick = 0f
      }
    }
    if (descending) floor += 1
    if (mainMenuing) Some(home)
    else if (descending) Some(new LevelGen(player, Some(this), world))
    else if (player.dead) Some(new GameOver(world, player.lastStrike))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    level.draw(batch)
    items.foreach(ite => ite.draw(batch))
    enemies.foreach(e => e.draw(batch))
    player.draw(batch)
    for (
      x <-
        -Trog.translationX - 5 to (-Trog.translationX + Geometry.ScreenWidth / screenUnit).toInt + 5
    ) {
      for (
        y <-
          -Trog.translationY - 5 to (-Trog.translationY + Geometry.ScreenHeight / screenUnit).toInt + 5
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
}
