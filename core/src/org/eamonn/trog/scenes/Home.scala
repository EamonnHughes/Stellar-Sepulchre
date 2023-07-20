package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class Home extends Scene {
  var cameraLocation: Vec2 = Vec2(0, 0)
  var genMap = new GeneratedMap(16)
  var doneGenerating = false
  var level = new Level

  override def init(): InputAdapter = {
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    if (!doneGenerating) { doneGenerating = genMap.generate() } else {
      if(level.walkables.isEmpty) level = genMap.doExport()
    }
    None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    Trog.translationX = cameraLocation.x
    Trog.translationY = cameraLocation.y
    if(!doneGenerating)genMap.draw(batch) else level.draw(batch)
  }
}
