package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.character.Player
import org.eamonn.trog.procgen.{Level, World}
import org.eamonn.trog.{Geometry, Scene, Text, Trog}
import org.eamonn.trog.scenes.Home

class GameOver(world: World, enemy: String) extends Scene {
  var done = false
  var home = new Home(world)
  override def init(): InputAdapter = {
    SaveLoad.saveState(new Game(new Level, new Player, world), 0)
    home.game = SaveLoad.loadState(0)
    home.world = home.game.world
    new GameOverInput(this)
  }

  override def updateCamera(): Unit = {}

  override def update(delta: Float): Option[Scene] = {
    if (done) Some(home) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s" You have perished at the hands of $enemy. \n Accept your fate: [ENTER]",
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
