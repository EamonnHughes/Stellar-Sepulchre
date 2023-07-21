package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class Home extends Scene {
  var player: Player = Player()

  override def init(): InputAdapter = {
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {

    if(player.ready) Some(new LevelGen(player)) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(batch, "Descend: [ENTER]", 0, Geometry.ScreenHeight/2)
  }
}
