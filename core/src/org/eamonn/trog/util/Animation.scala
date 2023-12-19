package org.eamonn.trog.util

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.screenUnit

object Animation {

  def twoFrameAnimation(game: Game, batch: PolygonSpriteBatch, string: String, x: Int, y: Int): Unit = {
    var number = if (game.animateTime >= .5f) "2" else "1"
    val texture = TextureWrapper.load(s"$string$number")
    batch.draw(texture, x*screenUnit, y*screenUnit, screenUnit, screenUnit)
  }

}
