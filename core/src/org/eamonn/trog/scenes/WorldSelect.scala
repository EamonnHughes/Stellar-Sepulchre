package org.eamonn.trog.scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.procgen.{Level, World}
import org.eamonn.trog.{
  Geometry,
  Player,
  SaveLoad,
  Scene,
  Text,
  Trog,
  screenUnit
}

class WorldSelect(home: Home) extends Scene {
  var done = false
  def worldOptions: List[String] = SaveLoad.worldList
  var selected = 0
  var world: World = SaveLoad.loadState(0, worldOptions(selected)).world
  override def init(): InputAdapter = {
    home.selecting = false
    new WorldSelectControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    world = SaveLoad.loadState(0, worldOptions(selected)).world
    home.world = world
    home.game = SaveLoad.loadState(0, world)
    if (done) Some(home) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      "New World: [N]",
      -Trog.translationX * screenUnit,
      (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight / 2) + (screenUnit * 4)
    )
    worldOptions.zipWithIndex.foreach({
      case (opt, i) => {
        if (selected == i) Text.mediumFont.setColor(Color.WHITE)
        else Text.mediumFont.setColor(Color.LIGHT_GRAY)
        Text.mediumFont.draw(
          batch,
          s"[${i + 1}] $opt",
          -Trog.translationX * screenUnit,
          (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight / 2) - (screenUnit * (i + 1))
        )
      }
    })
  }

  override def updateCamera(): Unit = {}
}
class WorldSelectControl(select: WorldSelect) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.DOWN) {
      select.selected = (select.selected + 1) % select.worldOptions.length
    } else if (keycode == Keys.UP) {
      select.selected =
        (select.selected + select.worldOptions.length - 1) % select.worldOptions.length
    } else if (keycode == Keys.ENTER) {
      select.done = true
    } else if (keycode == Keys.N) {
      SaveLoad.saveState(new Game(new Level, new Player, new World), 0)
    }
    true
  }
}
