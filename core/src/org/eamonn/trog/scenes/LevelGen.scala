package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

class LevelGen(
    player: Player,
    game: Option[Game],
    world: World
) extends Scene {
  var background: TextureWrapper = TextureWrapper.load("generate.png")
  var cameraLocation: Vec2 = Vec2(0, 0)
  var genMap = GeneratedMap(45, 6, 10, .2f)
  var doneGenerating = false
  var level = new Level

  override def init(): InputAdapter = {
    new LevelGenControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    while (!doneGenerating) doneGenerating = genMap.generate()

    if (level.walkables.isEmpty) {
      level = genMap.doExport()
      player.location = level.upLadder.copy()
      player.destination = level.upLadder.copy()
    }

    var gameNew = new Game(level, player, world)
    if (game.nonEmpty) {
      game.foreach(g => {
        gameNew = g
      })
    }
    gameNew.descending = false
    gameNew.enemies = List.empty
    gameNew.allSpawned = false
    gameNew.level = level
    gameNew.keysDown = List.empty
    gameNew.clicked = false
    gameNew.showingCharacterSheet = false

    if (doneGenerating && level.walkables.nonEmpty) {
      Some(gameNew)
    } else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    /*batch.draw(
      background,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )*/
    genMap.draw(batch)
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      "Generating",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
  }

  override def updateCamera(): Unit = {}
}
class LevelGenControl(gen: LevelGen) extends InputAdapter {}
