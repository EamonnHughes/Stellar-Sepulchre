package org.eamonnh.trog
package scenes

import com.badlogic.gdx.{Gdx, InputAdapter}
import com.badlogic.gdx.graphics.{Color, Pixmap}
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Scene
import org.eamonnh.trog.procgen.World
import org.eamonnh.trog.util.{Button, loadButton, playButton, quitButton}

class Home(wld: World) extends Scene {
  var world: World = wld
  var next = false
  var game: Game = _
  var gameLoaded = false
  override var realMouseLoc: Vec2 = Vec2(0, 0)
  override var mouseDownLoc: Option[Vec2] = None

  var mainColor = new Color(.48f, .69f, .37f, 1)
  def buttons: List[Button] = if(game.loadable) List(playButton(this), loadButton(this), quitButton(this)) else List(playButton(this), quitButton(this))
  def unavailableButtons: List[Button] = if(!game.loadable) List(loadButton(this)) else List.empty


  override def updateCamera(): Unit = {}

  override def init(): InputAdapter = {
    var pm = new Pixmap(Gdx.files.internal("Mouse.png"));
    Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0))
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
  }

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
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
    unavailableButtons.foreach(button => button.drawUnavailable(batch ,this))
  }
}
