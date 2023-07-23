package org.eamonn.trog

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

case class Player() {
  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }
  var playerIcon: TextureWrapper = TextureWrapper.load("charv1.png")
  var game: Game = _
  var ready = false
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  def draw(batch: PolygonSpriteBatch) = {
    batch.setColor(Color.WHITE)
    batch.draw(
      playerIcon,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  def update(delta: Float) = {
    if (!yourTurn) {
      tick += delta
      if (tick > .25f) {
        yourTurn = true
        tick = 0f
      }
    }
    if (yourTurn) {
      if (game.keysDown.contains(Keys.S)) {
        destination.y = location.y - 1
        destination.x = location.x
      } else if (game.keysDown.contains(Keys.W)) {
        destination.y = location.y + 1
        destination.x = location.x
      } else if (game.keysDown.contains(Keys.D)) {
        destination.y = location.y
        destination.x = location.x + 1
      } else if (game.keysDown.contains(Keys.A)) {
        destination.y = location.y
        destination.x = location.x - 1
      } else if (game.keysDown.contains(Keys.SPACE)) {
        yourTurn = false
      } else if (game.clicked) {
        destination = game.mouseLocOnGrid.copy()
      } else if (
        game.keysDown.contains(Keys.PERIOD) && (game.keysDown.contains(
          Keys.SHIFT_RIGHT
        ) || game.keysDown.contains(Keys.SHIFT_LEFT))
      ) {
        tryToGoDown()
      }
    }
    if (destination != location && yourTurn) {
      var path = Pathfinding.findPath(location, destination, game.level)
      path.foreach(p => {
        location = p.list.reverse(1).copy()
        yourTurn = false
      })
    }
  }
}