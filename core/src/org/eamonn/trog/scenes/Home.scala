package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}
import org.eamonn.trog.util.TextureWrapper

class Home(world: World) extends Scene {
  var background: TextureWrapper = TextureWrapper.load("sepulctbg.png")
  var timage: TextureWrapper = TextureWrapper.load("TitleImage.png")
  var next = false

  override def init(): InputAdapter = {
    new HomeControl(this)
  }
  override def update(delta: Float): Option[Scene] = {

    if (next) Some(new CharCreation(world)) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    batch.setColor(Color.WHITE)

    batch.draw(
      background,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )
    batch.draw(
      timage,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - screenUnit*8,
      screenUnit*1600/64,
      screenUnit*8
    )
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.largeFont.setColor(Color.WHITE)
    Text.largeFont.draw(
      batch,
      "Descend: [ENTER]",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
  }
}
