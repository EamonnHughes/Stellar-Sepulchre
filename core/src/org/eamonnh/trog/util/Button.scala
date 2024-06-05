package org.eamonnh.trog.util

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.scenes.Home
import org.eamonnh.trog.{Geometry, Scene, Text, Trog, Vec2, screenUnit}

trait Button {
  var brightness: Float = 1f
  var tooltip: String
  def checkForHover(scene: Scene): Boolean = {
    if (
      (scene.realMouseLoc.x/screenUnit >= location.x && scene.realMouseLoc.x/screenUnit < location.x + size.x) &&
        (scene.realMouseLoc.y/screenUnit >= location.y && scene.realMouseLoc.y/screenUnit < location.y + size.y)) true else false
  }

  def location: Vec2

  def size: Vec2

  def upIcon: TextureWrapper
  def downIcon: TextureWrapper

  def onClick(): Unit

  def checkForClick(scene: Scene): Boolean = {
    if (
      (scene.realMouseLoc.x/screenUnit >= location.x && scene.realMouseLoc.x/screenUnit < location.x + size.x) &&
        (scene.realMouseLoc.y/screenUnit >= location.y && scene.realMouseLoc.y/screenUnit < location.y + size.y) &&
        scene.mouseDownLoc.forall(mDLoc => {
          (mDLoc.x/screenUnit >= location.x && mDLoc.x/screenUnit < location.x + size.x) &&
            (mDLoc.y/screenUnit >= location.y && mDLoc.y/screenUnit < location.y + size.y)
        })

    ) {
      onClick()
      true
    } else false
  }

  def checkForHold(scene: Scene): Boolean = {
    scene.mouseDownLoc.nonEmpty &&
    (scene.realMouseLoc.x/screenUnit >= location.x && scene.realMouseLoc.x/screenUnit < location.x + size.x) &&
      (scene.realMouseLoc.y/screenUnit >= location.y && scene.realMouseLoc.y/screenUnit < location.y + size.y) &&
      scene.mouseDownLoc.forall(mDLoc => {
        (mDLoc.x/screenUnit >= location.x && mDLoc.x/screenUnit < location.x + size.x) &&
          (mDLoc.y/screenUnit >= location.y && mDLoc.y/screenUnit < location.y + size.y)
      })
  }

  def draw(batch: PolygonSpriteBatch, scene: Scene): Unit = {
    if(checkForHover(scene)) brightness = .9f  else brightness = 1f
    batch.setColor(brightness, brightness, brightness, 1f)
    if(checkForHold(scene)) {
      batch.draw(
        downIcon,
        location.x * screenUnit,
        location.y * screenUnit,
        size.x * screenUnit,
        size.y * screenUnit)
    } else {
      batch.draw(
      upIcon,
      location.x * screenUnit,
      location.y * screenUnit,
      size.x * screenUnit,
      size.y * screenUnit)
    }
    if(checkForHover(scene) && tooltip != ""){
      Text.smallFont.draw(batch, tooltip, scene.realMouseLoc.x, scene.realMouseLoc.y, screenUnit * 5, 0, true)
    }
  }

  def drawUnavailable(batch: PolygonSpriteBatch, scene: Scene): Unit = {
    batch.setColor(.5f, .5f, .5f, 1f)
      batch.draw(
        upIcon,
        location.x * screenUnit,
        location.y * screenUnit,
        size.x * screenUnit,
        size.y * screenUnit)
  }
}



case class playButton(home: Home) extends Button {
  override def location: Vec2 = Vec2(
    (Geometry.ScreenWidth / screenUnit).toInt / 2 - 2,
    6
  )

  override def size: Vec2 = Vec2(4, 2)

  override def downIcon: TextureWrapper = TextureWrapper.load("PlayButtonDown.png")
  override def upIcon: TextureWrapper = TextureWrapper.load("PlayButton.png")


  override def onClick(): Unit = {
    home.next = true
  }

  override var tooltip: String = ""
}


case class loadButton(home: Home) extends Button {
  override def location: Vec2 = Vec2(
    (Geometry.ScreenWidth / screenUnit).toInt / 2 - 2,
    4
  )

  override def size: Vec2 = Vec2(4, 2)

  override def downIcon: TextureWrapper = TextureWrapper.load("LoadButtonDown.png")
  override def upIcon: TextureWrapper = TextureWrapper.load("LoadButton.png")


  override def onClick(): Unit = {
    home.gameLoaded = true
  }

  override var tooltip: String = ""
}


case class quitButton(home: Home) extends Button {
  override def location: Vec2 = Vec2(
    (Geometry.ScreenWidth / screenUnit).toInt / 2 - 2,
    2
  )

  override def size: Vec2 = Vec2(4, 2)

  override def downIcon: TextureWrapper = TextureWrapper.load("QuitButtonDown.png")
  override def upIcon: TextureWrapper = TextureWrapper.load("QuitButton.png")


  override def onClick(): Unit = {
    System.exit(0)
  }

  override var tooltip: String = ""
}
