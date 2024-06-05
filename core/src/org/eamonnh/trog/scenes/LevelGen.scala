package org.eamonnh.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Scene
import org.eamonnh.trog.Trog.loadBG
import org.eamonnh.trog.character.Player
import org.eamonnh.trog.inGameUserInterface.inCharacterSheet
import org.eamonnh.trog.procgen._

class LevelGen(
    player: Player,
    game: Option[Game],
    world: World
) extends Scene {
  var cameraLocation: Vec2 = Vec2(0, 0)
  var genMap = GeneratedMap(30, 4, 6, .2f)
  var doneGenerating = false
  var level = new Level

  override def init(): InputAdapter = {
    new LevelGenControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    while (!doneGenerating) doneGenerating = genMap.generate()
    genMap.potentialDoorways.foreach(place1 => {
      if (
        (!genMap.rooms.exists(r => {
          r.getAllTiles.contains(Vec2(place1.x, place1.y + 1))
        }) && !genMap.rooms.exists(r => {
          r.getAllTiles.contains(Vec2(place1.x, place1.y - 1))
        })) ||
        (!genMap.rooms.exists(r => {
          r.getAllTiles.contains(Vec2(place1.x + 1, place1.y))
        }) && !genMap.rooms.exists(r => {
          r.getAllTiles.contains(Vec2(place1.x - 1, place1.y))
        }))
      ) {
        genMap.locPurpose.addOne((place1, Door()))
      }
    })
    if (level.terrains.forall(_._1.isInstanceOf[Emptiness])) {
      level = genMap.doExport()
      player.location = level.upLadder.copy()
      player.destination = level.upLadder.copy()
    }

    var gameNew = new Game(level, player, world)
    gameNew.loadable = true
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
    inCharacterSheet = false
    gameNew.explored = List.empty
    gameNew.items = gameNew.items.filter(i =>
      i.possessor.nonEmpty && i.possessor.head.isInstanceOf[Player]
    )

    if (
      doneGenerating && level.terrains.exists(!_._1.isInstanceOf[Emptiness])
    ) {
      Some(gameNew)
    } else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(
      loadBG,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )
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

  override var realMouseLoc: Vec2 = Vec2(0, 0)
  override var mouseDownLoc: Option[Vec2] = None
}

class LevelGenControl(gen: LevelGen) extends InputAdapter {}
