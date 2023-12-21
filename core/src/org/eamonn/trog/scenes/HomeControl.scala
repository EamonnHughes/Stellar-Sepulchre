package org.eamonn.trog
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
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {

    true
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Keys.ENTER) {
      if (home.selected == 0) home.next = true
      if (home.selected == 1) home.gameLoaded = true
      if (home.selected == 2) System.exit(0)
    }
    if (keycode == Keys.UP) {
      if (home.selected == 2 && !home.game.loadable) home.selected = 0 else if (home.selected == 0) home.selected = home.itemNums - 1 else home.selected -= 1
    }
    if (keycode == Keys.DOWN) {
      if (home.selected == 0 && !home.game.loadable) home.selected = 2 else if (home.selected == home.itemNums - 1) home.selected = 0 else home.selected += 1
    }

    true
  }
}
