package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.scenes.Game

case class Player() {
  var game: Game = _
  var ready = false
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  def draw(batch: PolygonSpriteBatch) = {
    batch.setColor(Color.RED)
    batch.draw(
      Trog.Square,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  def update(delta: Float) = {
    yourTurn = true
    if(destination != location && yourTurn) {
      var path = Pathfinding.findPath(location, destination, game.level)
      path.foreach(p => {
        location = p.list.head.copy()
        yourTurn = false
      })
    }
  }
}
