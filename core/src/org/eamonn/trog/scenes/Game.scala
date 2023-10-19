package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import jdk.javadoc.internal.doclets.formats.html.markup.Navigation
import org.eamonn.trog.SaveLoad.{loadState, saveState}
import org.eamonn.trog.Scene
import org.eamonn.trog.character.Player
import org.eamonn.trog.items.Item
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}

import scala.util.Random

class Game(lvl: Level, plr: Player, wld: World)
    extends Scene
    with Serializable {
  def addMessage(message: String): Unit = {
    messages = message :: messages
  }
  var clickedForTargeting = false
  var saveTick = 0f
  var explored: List[Vec2] = List.empty
  var loadable = false
  var messages = List.empty[String]
  var world = wld
  var keysDown: List[Int] = List.empty
  var inCharacterSheet = false
  var inInventory = false
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
  def home: Home = {
    var h = new Home(world)
    h.game = loadState(0)
    h
  }
  override def init(): InputAdapter = {
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
  def drawConsole(batch: PolygonSpriteBatch): Unit = {
    var log = messages
      .take(5)
      .reverse
      .mkString("\n")

    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(
      batch,
      log,
      (-Trog.translationX * screenUnit),
      ((-Trog.translationY + 2) * screenUnit)
    )

  }
  def renderUI(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    player.stats.skills.zipWithIndex.foreach({
      case (s, i) => {
        batch.draw(
          s.icon,
          -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
          -Trog.translationY * screenUnit,
          screenUnit * 1.5f,
          screenUnit * 1.5f
        )
        Text.smallFont.draw(
          batch,
          s"${i + 1}",
          -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
          -Trog.translationY * screenUnit + (screenUnit * 1.83f)
        )
        if (s.ccd > 0) {
          Text.hugeFont.setColor(1f, 1f, 1f, 0.5f)
          Text.hugeFont.draw(
            batch,
            s"${s.ccd.toString}",
            -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
            -Trog.translationY * screenUnit + (screenUnit * 1.5f)
          )
        }
      }
    })
    Text.mediumFont.draw(
      batch,
      s"${player.name}, the level ${player.stats.level} ${player.archetype.name} on floor ${floor}",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight
    )
    batch.setColor(Color.YELLOW)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4,
      screenUnit / 8
    )
    batch.setColor(Color.ORANGE)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4 * player.stats.exp / player.stats.nextExp,
      screenUnit / 8
    )
    batch.setColor(Color.FIREBRICK)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4,
      screenUnit / 2
    )
    batch.setColor(Color.RED)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4 * player.stats.health / player.stats.maxHealth,
      screenUnit / 2
    )
    batch.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s"${player.stats.health}/${player.stats.maxHealth}",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - screenUnit
    )
    drawConsole(batch)
    if (inCharacterSheet) {
      batch.setColor(0f, 0f, 0f, .5f)
      batch.draw(
        Trog.Square,
        -Trog.translationX * screenUnit + (2 * screenUnit),
        -Trog.translationY * screenUnit + (2 * screenUnit),
        (Geometry.ScreenWidth - (4 * screenUnit)),
        (Geometry.ScreenHeight - (4 * screenUnit))
      )
      Text.mediumFont.setColor(Color.WHITE)
      def uiHelmName: String = {
        "None"
      }
      def uiArmorName: String = {
        "None"
      }
      def uiWeaponName: String = {
        var str = "None"
        player.equipment.weapon.foreach(w => str = w.name)
        str
      }
      Text.mediumFont.draw(
        batch,
        s" " +
          s"Name: ${player.name}\n " +
          s"Archetype: ${player.archetype.name}\n " +
          s"Level: ${player.stats.level}\n " +
          s"Experience: ${player.stats.exp}/${player.stats.nextExp}\n " +
          s"Floor: $floor\n " +
          s"Health: ${player.stats.health}/${player.stats.maxHealth}\n " +
          s"Armor Class: ${player.stats.ac}\n " +
          s"Sight Radius: ${player.stats.sightRad}\n " +
          s"Attack Bonus: ${player.stats.attackMod}\n " +
          s"Damage Bonus: ${player.stats.damageMod}\n " +
          s"Crit Modifier: %${player.stats.critMod * 100}\n " +
          s"Crit Chance: %${player.stats.critChance}\n\n " +
          s"Helm: ${uiHelmName}\n " +
          s"Body Armor: ${uiArmorName}\n " +
          s"Weapon: ${uiWeaponName}",
        -Trog.translationX * screenUnit + (2 * screenUnit),
        (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
      )
    } else if (inInventory) {
      batch.setColor(0f, 0f, 0f, .5f)
      batch.draw(
        Trog.Square,
        -Trog.translationX * screenUnit + (2 * screenUnit),
        -Trog.translationY * screenUnit + (2 * screenUnit),
        (Geometry.ScreenWidth - (4 * screenUnit)),
        (Geometry.ScreenHeight - (4 * screenUnit))
      )
      var inv: String = ""
      items
        .filter(i => i.possessor.nonEmpty && i.possessor.head == player)
        .filter(n => n.tNum >= 1)
        .zipWithIndex
        .foreach({
          case (item, index) => {
            if (index == player.inventoryItemSelected) {
              inv = s"$inv \n - x${item.tNum} ${item.name}"
            } else {
              inv = s"$inv \n x${item.tNum} ${item.name}"
            }
          }
        })
      Text.mediumFont.draw(
        batch,
        s"INVENTORY:\n$inv",
        -Trog.translationX * screenUnit + (2 * screenUnit),
        (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
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
    if (keycode == Keys.C) {
      game.inCharacterSheet = !game.inCharacterSheet
      game.inInventory = false
    } else if (keycode == Keys.I) {
      game.inInventory = !game.inInventory
      game.inCharacterSheet = false

    }
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    game.mouseLocOnGrid.x =
      (screenX / screenUnit).floor.toInt - Trog.translationX
    game.mouseLocOnGrid.y =
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt - Trog.translationY
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
