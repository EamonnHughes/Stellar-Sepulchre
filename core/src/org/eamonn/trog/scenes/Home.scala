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

  var mainColor = new Color(.48f, .69f, .37f, 1)
  var darkColor = new Color(.28f, .49f, .17f, 1)

  override def updateCamera(): Unit = {}

  override def init(): InputAdapter = {
    Trog.translationX = 0
    Trog.translationY = 0
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    if (selecting) Some(new WorldSelect(this))
    else if (gameLoaded && game.loadable) Some(game)
    else if (next) Some(new CharCreation(world))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(.8f, .8f, .8f, 1)
    batch.draw(
      Trog.homeBG,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )
    batch.setColor(mainColor)

    batch.draw(
      Trog.titleIMG,
      -Trog.translationX * textUnit,
      -Trog.translationY * textUnit + Geometry.ScreenHeight - textUnit * 5,
      Geometry.ScreenWidth * 3 / 4,
      (Geometry.ScreenWidth / 200) * 64 * 3 / 4
    )
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.largeFont.setColor(mainColor)
    Text.largeFont.draw(
      batch,
      "  Descend: [ENTER]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 3 / 4
    )
    if (!game.loadable) Text.largeFont.setColor(darkColor)
    Text.largeFont.draw(
      batch,
      "\n  Load Last Save: [L]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 3 / 4
    )
    Text.largeFont.setColor(mainColor)
    Text.largeFont.draw(
      batch,
      "\n\n  New World: [W]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 3 / 4
    )
  }
}
