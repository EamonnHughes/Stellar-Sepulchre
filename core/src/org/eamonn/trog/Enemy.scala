package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.util.TextureWrapper

trait Enemy {
  var location: Vec2 = Vec2(0, 0)
  var health: Int
  var texture: TextureWrapper
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)
    batch.draw(texture, location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
}

case class IceImp() extends Enemy {
  var health = 5
  var texture = TextureWrapper.load("iceimp.png")
}
