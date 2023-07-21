package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

case class Player () {
var ready = false
  var location: Vec2 = Vec2(0, 0)
  def draw(batch: PolygonSpriteBatch) = {
    batch.setColor(Color.RED)
    batch.draw(Trog.Square, location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
}
