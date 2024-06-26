package org.eamonnh.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.{Gdx, InputAdapter}
import com.badlogic.gdx.graphics.{Color, Pixmap}
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.SaveLoad.{loadState, saveState}
import org.eamonnh.trog.Scene
import org.eamonnh.trog.character.Player
import org.eamonnh.trog.items.Item
import org.eamonnh.trog.procgen.{Emptiness, Level, World}

import scala.util.Random

class Game(lvl: Level, plr: Player, wld: World)
    extends Scene
    with Serializable {
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
  var playerTurnDone = false
  var floor = 1
  var updatingCameraX = false
  var updatingCameraY = false
  var mainMenuing = false
  var allSpawned = false
  var clicked = false
  override var realMouseLoc: Vec2 = Vec2(0,0)
  override var mouseDownLoc: Option[Vec2] = None
  var mouseLocOnGrid: Vec2 = Vec2(0, 0)
  var enemies: List[Enemy] = List.empty
  var items: List[Item] = List.empty
  var animateTime = 0f
  var timebetweenAnimations = 0f
  var lvlupEffect = 0f
  var lvlUping = false

  override def renderUI(batch: PolygonSpriteBatch): Unit =
    inGameUserInterface.renderUI(batch, this)

  def addMessage(message: String): Unit = {
    messages = message :: messages
  }

  override def init(): InputAdapter = {
    var pm = new Pixmap(Gdx.files.internal("Nothing.png"))
    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0))
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

    if (lvlUping) {
      lvlupEffect += (delta * 2)
      if (lvlupEffect > .5f) {
        lvlUping = false
        lvlupEffect = 0
        if(player.perkPool.exists(p => p.isAllowed(player))) {
          player.menuItemSelected = 0
          player.inPerkChoice = true
        }
      }
    } else {
      if (lvlupEffect > 0) lvlupEffect -= (delta / 4)
      if (lvlupEffect < 0) lvlupEffect = 0
    }
    mouseLocOnGrid.x = (fakeLoc.x / screenUnit).floor.toInt - Trog.translationX
    mouseLocOnGrid.y =
      ((Geometry.ScreenHeight - fakeLoc.y) / screenUnit).floor.toInt - Trog.translationY
    timebetweenAnimations += delta
    if (timebetweenAnimations > .5f) {
      animateTime = animateTime + delta
    }
    if (animateTime >= .5f) {
      animateTime = 0
      timebetweenAnimations = 0
    }
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
        var loc = level.terrains.zipWithIndex.filterNot({ case ((w, n), i) =>
          player.location == getVec2fromI(i, level) || enemies.exists(e =>
            e.location == getVec2fromI(i, level)
          ) || !(w.walkable && !w.isInstanceOf[Emptiness])
        })(
          Random.nextInt(
            level.terrains.zipWithIndex
              .filterNot({ case ((w, n), i) =>
                player.location == getVec2fromI(i, level) || enemies.exists(e =>
                  e.location == getVec2fromI(i, level)
                ) || !(w.walkable && !w.isInstanceOf[Emptiness])
              })
              .length
          )
        )
        var enemy = Servitor()
        enemy.initialize(this, getVec2fromI(loc._2, level))
      }
      allSpawned = true
    }
    if (player.rangedSkillUsing.nonEmpty) {
      player.rangedSkillUsing.foreach(a => player.doRangedSkill(a))
    } else {
      player.update(delta)
      enemies.foreach(e => {
        if (e.stats.health <= 0) {
          e.Die()
        }
      })
      enemies.foreach(e => e.update(delta))
      if (playerTurnDone) {
        saveTick += 1
        enemies.foreach(_.turn = true)
        player.stats.skills.foreach(sk => if (sk.ccd > 0) sk.ccd -= 1)
        playerTurnDone = false
      }
      if (saveTick >= 25) {
        var kd = keysDown
        saveState(this, 0)
        keysDown = kd
        saveTick = 0f
      }
    }
    if (player.dead) {
      Trog.inGameOST.stop()
      Trog.Tolling.play(.5f)
    }
    if (descending) floor += 1
    if (mainMenuing) Some(home)
    else if (descending) Some(new LevelGen(player, Some(this), world))
    else if (player.dead) Some(new GameOver(world, player.lastStrike))
    else None
  }

  def home: Home = {
    var h = new Home(world)
    h.game = loadState(0)
    h
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    level.draw(batch)
    items.foreach(ite => ite.draw(batch))
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
            Pathfinding.findRaycastPathUpTo(player.location, Vec2(x, y), level)
          if (path.nonEmpty) {
            path.foreach(p => {
              dist = p.list.length
            })
          }
        }
        if (dist >= player.stats.sightRad && (Vec2(x, y) != player.location)) {
          if (
            explored.contains(Vec2(x, y)) || Vec2(x, y).getAdjacents.exists(
              adj => explored.contains(adj)
            )
          ) batch.setColor(0, 0, 0, .825f)
          else batch.setColor(0, 0, 0, 1)
        } else {
          enemies
            .filter(e => e.location == Vec2(x, y))
            .foreach(e => e.draw(batch))
          var lightLevel: Float = {
            ((((player.stats.sightRad - dist).toFloat / player.stats.sightRad) + .25f) min 1) max 0
          }
          if(Vec2(x, y) == player.location) {
            batch.setColor(Color.CLEAR)
          } else {
            batch.setColor(
            0,
            0,
            0,
            1 - lightLevel
          )
          }
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
