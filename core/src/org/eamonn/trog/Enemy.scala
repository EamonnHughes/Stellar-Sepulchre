package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.{Square, garbage}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

trait Enemy {
  var game: Game
  var location: Vec2 = Vec2(0, 0)
  var health: Int = Int.MaxValue
  var maxHealth: Int
  var ac: Int
  var texture: TextureWrapper
  def update(delta: Float): Unit
  def draw(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.RED)
    batch.draw(Square, location.x * screenUnit, location.y * screenUnit, screenUnit * health/maxHealth, screenUnit*.1f)
    batch.setColor(Color.WHITE)
    batch.draw(texture, location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
}

case class IceImp() extends Enemy {
  var game: Game = _
  var maxHealth = 5
  var ac = 5
  var texture = TextureWrapper.load("iceimp.png")

  override def update(delta: Float): Unit = {
    if(health <= 0){
      game.enemies = game.enemies.filterNot(e => e eq this)
      game.player.exp += 5
    }
  }
}
