package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.procgen.World
import org.eamonn.trog.{Geometry, Scene, Text, Trog}
import org.eamonn.trog.scenes.Home

class GameOver(world: World) extends Scene {
  var done = false
  override def init(): InputAdapter = {
    new GameOverInput(this)
  }

  override def updateCamera(): Unit = {}

  override def update(delta: Float): Option[Scene] = {
    if (done) Some(new Home(world)) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      " You have perished. \n Accept your fate: [ENTER]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
  }
}
class GameOverInput(over: GameOver) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ENTER) over.done = true
    true
  }
}
