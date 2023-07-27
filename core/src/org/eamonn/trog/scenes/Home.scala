package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}

class Home(world: World) extends Scene {
  var next = false

  override def init(): InputAdapter = {
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {

    if(next) Some(new CharCreation(world)) else None
  }


  override def render(batch: PolygonSpriteBatch): Unit = {

  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(batch, " LASCIATE OGNI SPERANZA, VOI CH'ENTRATE\n Descend: [ENTER]", -Trog.translationX*screenUnit,-Trog.translationY*screenUnit + Geometry.ScreenHeight/2)
  }
}
