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
}
