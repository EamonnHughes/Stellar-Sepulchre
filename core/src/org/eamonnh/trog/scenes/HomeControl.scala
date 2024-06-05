package org.eamonnh.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

class HomeControl(home: Home) extends InputAdapter {
  override def touchDown(
      screenX: Int,
      screenY: Int,
      pointer: Int,
      button: Int
  ): Boolean = {
    var loc: Vec2 = Vec2(
      (screenX / screenUnit).floor.toInt,
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt
    )
    home.realMouseLoc = Vec2(
      screenX,
      (Geometry.ScreenHeight - screenY).toInt,
    )
    home.mouseDownLoc = Some(Vec2(
      screenX,
      (Geometry.ScreenHeight - screenY).toInt,
    ))
    true
  }


  override def touchDragged(
                             screenX: Int,
                             screenY: Int,
                             pointer: Int
                           ): Boolean = {
    var loc: Vec2 = Vec2(
      (screenX / screenUnit).floor.toInt,
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt
    )
    home.realMouseLoc = Vec2(
      screenX,
      (Geometry.ScreenHeight - screenY).toInt,
    )
    home.buttons.foreach(b => {
      b.checkForHold(home)
    })
    true
  }


  override def touchUp(
                        screenX: Int,
                        screenY: Int,
                        pointer: Int,
                        button: Int
                      ): Boolean = {
    var loc: Vec2 = Vec2(
      (screenX / screenUnit).floor.toInt,
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt
    )
    home.buttons.foreach(b => {
      b.checkForClick(home)
    })
    home.mouseDownLoc = None
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    home.realMouseLoc = Vec2(
      screenX,
      (Geometry.ScreenHeight - screenY).toInt,
    )

    true
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ENTER) {
      if (home.selected == 0) home.next = true
      if (home.selected == 1) home.gameLoaded = true
      if (home.selected == 2) System.exit(0)
    }
    if (keycode == Keys.UP) {
      if (home.selected == 2 && !home.game.loadable) home.selected = 0
      else if (home.selected == 0) home.selected = home.itemNums - 1
      else home.selected -= 1
    }
    if (keycode == Keys.DOWN) {
      if (home.selected == 0 && !home.game.loadable) home.selected = 2
      else if (home.selected == home.itemNums - 1) home.selected = 0
      else home.selected += 1
    }

    true
  }
}
