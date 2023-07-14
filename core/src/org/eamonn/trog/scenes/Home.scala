package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.GeneratedMap

class Home extends Scene {
  var genMap = new GeneratedMap(40)

  override def init(): InputAdapter = {
    genMap.generate()

    new HomeControl(this)
  }
var tick = 0f
  override def update(delta: Float): Option[Scene] = {
    tick += delta
    if(tick >= .25f) {
      if (genMap.rooms.exists(r => genMap.rooms.exists(r2 => r.doesOverlap(r2)))) {
        genMap.rooms.foreach(r => {
          var dX = 0
          var dY = 0
          genMap.rooms.filter(ro => ro.doesOverlap(r)).foreach(ro => {
            dX += (r.location.x - ro.location.x).sign
            dY += (r.location.y - ro.location.y).sign
          })
          if (!genMap.rooms.exists(ro => ro.doesOverlap(r))){

          }else{
            if (dX != 0) r.location.x += dX.sign
            if (dY != 0) r.location.y += dY.sign
          }

        })
      }
      tick = 0f
    }
    None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    genMap.draw(batch)
  }
}
