package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}
import org.eamonn.trog.util.TextureWrapper

class Home(wld: World) extends Scene {
  var world: World = wld
  var next = false
  var game: Game = _
  var gameLoaded = false
  var selecting = false

  override def updateCamera(): Unit = {}

  override def init(): InputAdapter = {
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {

    if(selecting) Some(new WorldSelect(this))
    else if (gameLoaded && game.loadable) Some(game)
    else if (next) Some(new CharCreation(world))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)

    batch.draw(
      Trog.homeBG,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )
    batch.draw(
      Trog.titleIMG,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - screenUnit * 8,
      Geometry.ScreenWidth,
      (Geometry.ScreenWidth/200)*64
    )
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.largeFont.setColor(Color.WHITE)
    Text.largeFont.draw(
      batch,
      "  Descend: [ENTER]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
    if (!game.loadable) Text.largeFont.setColor(Color.GRAY)
    Text.largeFont.draw(
      batch,
      "\n\n  Load Last Save: [L]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
    Text.largeFont.setColor(Color.WHITE)
    Text.largeFont.draw(
      batch,
      "\n\n\n\n  New World: [W]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
  }
}
