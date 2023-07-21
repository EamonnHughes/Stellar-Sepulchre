package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class Game(level: Level, player: Player) extends Scene {
  var cameraLocation: Vec2 = Vec2(0, 0)
  override def init(): InputAdapter = {
    new GameControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    println(level.walkables)
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
    if(keycode == Keys.UP) game.cameraLocation.y -= 1
    if(keycode == Keys.DOWN) game.cameraLocation.y += 1
    if(keycode == Keys.LEFT) game.cameraLocation.x += 1
    if(keycode == Keys.RIGHT) game.cameraLocation.x -= 1

    true
  }
  }
