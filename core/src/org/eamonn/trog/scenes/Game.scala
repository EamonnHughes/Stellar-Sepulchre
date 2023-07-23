package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class Game(lvl: Level, plr: Player) extends Scene {
  var keysDown: List[Int] = List.empty
  var level: Level = lvl
  var descending = false
  var player: Player = plr
  var cameraLocation: Vec2 = Vec2(0, 0)
  var updatingCameraX = false
  var updatingCameraY = false
  var clicked = false
  var mouseLocOnGrid: Vec2 = Vec2(0, 0)
  override def init(): InputAdapter = {
    player.game = this
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
    if (
      player.location.x < -cameraLocation.x + 5 || player.location.x > -cameraLocation.x + (Geometry.ScreenWidth / screenUnit) - 5
    ) updatingCameraX = true
    if (
      player.location.y < -cameraLocation.y + 5 || player.location.y > -cameraLocation.y + (Geometry.ScreenHeight / screenUnit) - 5
    ) updatingCameraY = true
    if (updatingCameraX || updatingCameraY) updateCamera()
    player.update(delta)
    if (descending) Some(new LevelGen(player, Some(this)))
    else
      None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    Trog.translationX = cameraLocation.x
    Trog.translationY = cameraLocation.y
    level.draw(batch)
    player.draw(batch)
  }
}
class GameControl(game: Game) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    game.keysDown = keycode :: game.keysDown
    true
  }
  override def keyUp(keycode: Int): Boolean = {
    game.keysDown = game.keysDown.filterNot(f => f == keycode)
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