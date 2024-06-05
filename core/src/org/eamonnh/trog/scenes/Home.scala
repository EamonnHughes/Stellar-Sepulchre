package org.eamonnh.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Scene
import org.eamonnh.trog.procgen.World
import org.eamonnh.trog.util.{Button, playButton}

class Home(wld: World) extends Scene {
  var world: World = wld
  var next = false
  var game: Game = _
  var gameLoaded = false
  var selected = 0
  var itemNums = 3
  override var realMouseLoc: Vec2 = Vec2(0, 0)
  override var mouseDownLoc: Option[Vec2] = None

  var mainColor = new Color(.48f, .69f, .37f, 1)
  var darkColor = new Color(.28f, .49f, .17f, 1)
  var buttons: List[Button] = List(playButton(this))

  override def updateCamera(): Unit = {}

  override def init(): InputAdapter = {
    Trog.translationX = 0
    Trog.translationY = 0
    new HomeControl(this)
  }

  override def update(delta: Float): Option[Scene] = {

    if (gameLoaded && game.loadable) Some(game)
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
    Text.hugeFont.setColor(mainColor)

    Text.hugeFont.draw(
      batch,
      " Stellar Sepulchre",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + (Geometry.ScreenHeight * .975f)
    )
    if (mouseDownLoc.isEmpty) buttons.foreach(_.brightness = 1f)
    batch.setColor(Color.WHITE)
    buttons.foreach(button => button.draw(batch, this))
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    if (selected == 0) Text.largeFont.setColor(mainColor)
    else Text.largeFont.setColor(darkColor)
    Text.largeFont.draw(
      batch,
      "  Descend",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 4 / 5
    )
    if (selected == 1) Text.largeFont.setColor(mainColor)
    else if (game.loadable) Text.largeFont.setColor(darkColor)
    else Text.largeFont.setColor(Color.GRAY)
    Text.largeFont.draw(
      batch,
      "\n  Load Last Save",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 4 / 5
    )
    if (selected == 2) Text.largeFont.setColor(mainColor)
    else Text.largeFont.setColor(darkColor)
    Text.largeFont.draw(
      batch,
      "\n\n  Quit",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight * 4 / 5
    )
  }

}
