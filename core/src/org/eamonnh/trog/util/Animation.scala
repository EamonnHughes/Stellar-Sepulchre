package org.eamonnh.trog.util

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.screenUnit

object Animation {

  def twoFrameAnimation(
      game: Game,
      batch: PolygonSpriteBatch,
      string: String,
      x: Float,
      y: Float
  ): Unit = {
    var number = if (game.animateTime >= .5f) "2" else "1"
    val texture = TextureWrapper.load(s"$string$number.png")
    batch.draw(texture, x * screenUnit, y * screenUnit, screenUnit, screenUnit)
  }

  def fourFrameAnimation(
      game: Game,
      batch: PolygonSpriteBatch,
      string: String,
      x: Float,
      y: Float
  ): Unit = {
    var number =
      if (game.animateTime >= .75f) "4"
      else if (game.animateTime >= .5f) "3"
      else if (game.animateTime >= .25) "2"
      else "1"
    val texture = TextureWrapper.load(s"$string$number.png")
    batch.draw(texture, x * screenUnit, y * screenUnit, screenUnit, screenUnit)
  }

}
