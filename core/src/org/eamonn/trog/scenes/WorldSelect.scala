package org.eamonn.trog.scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.character.Player
import org.eamonn.trog.procgen.{Level, World}
import org.eamonn.trog.{
  Geometry,
  SaveLoad,
  Scene,
  Text,
  Trog,
  screenUnit
}

class WorldSelect(home: Home) extends Scene {
  var done = false
  var world: World = SaveLoad.loadState(0).world
  override def init(): InputAdapter = {
    home.selecting = false
    new WorldSelectControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    if(done) {
      home.world = world
      home.game = SaveLoad.loadState(0)
    }
    if (done) Some(home) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      "Are you absolutely certain you want to delete your current \nworld and start a new one? This action is permanent, and will \nerase all progress you have made in the game.\n[y/n]",
      -Trog.translationX * screenUnit,
      (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight / 2) + (screenUnit * 4)
    )
  }

  override def updateCamera(): Unit = {}
}
class WorldSelectControl(select: WorldSelect) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.Y) {
      SaveLoad.saveState(new Game(new Level, new Player, new World), 0)
      select.world = SaveLoad.loadState(0).world
      select.done = true
    } else if(keycode == Keys.N) {
      select.done = true
    }
    true
  }
}
