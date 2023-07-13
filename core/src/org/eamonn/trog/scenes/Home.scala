package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.GeneratedMap

class Home extends Scene {
  var genMap = new GeneratedMap(20)

  override def init(): InputAdapter = {
    genMap.generate()

    new HomeControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    genMap.draw(batch)
  }
}
