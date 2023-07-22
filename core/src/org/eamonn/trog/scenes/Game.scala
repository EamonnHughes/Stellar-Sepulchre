package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class Game(lvl: Level, plr: Player) extends Scene {
  var level: Level = lvl
  var player: Player = plr
  var cameraLocation: Vec2 = Vec2(0, 0)
  var updatingCameraX = false
  var updatingCameraY = false
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
  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Keys.S) {
      game.player.destination.y = game.player.location.y - 1
      game.player.destination.x = game.player.location.x
    }
    if (keycode == Keys.W) {
      game.player.destination.y = game.player.location.y + 1
      game.player.destination.x = game.player.location.x
    }
    if (keycode == Keys.D) {
      game.player.destination.x = game.player.location.x + 1
      game.player.destination.y = game.player.location.y
    }
    if (keycode == Keys.A) {
      game.player.destination.x = game.player.location.x - 1
      game.player.destination.y = game.player.location.y
    }
    true
  }
}
