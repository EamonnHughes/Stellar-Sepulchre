package org.eamonn.trog.scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import org.eamonn.trog.inGameUserInterface.{inCharacterSheet, inInventory}
import org.eamonn.trog.{Geometry, Trog, screenUnit}

class GameControl(game: Game) extends InputAdapter {
  override def keyDown(keycode: Int): Boolean = {
    game.keysDown = keycode :: game.keysDown
    true
  }
  override def keyUp(keycode: Int): Boolean = {
    game.keysDown = game.keysDown.filterNot(f => f == keycode)
    if (keycode == Keys.C) {
      inCharacterSheet = !inCharacterSheet
      inInventory = false
    } else if (keycode == Keys.I) {
      inInventory = !inInventory
      inCharacterSheet = false

    }
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = {
    game.mouseLocOnGrid.x =
      (screenX / screenUnit).floor.toInt - Trog.translationX
    game.mouseLocOnGrid.y =
      ((Geometry.ScreenHeight - screenY) / screenUnit).floor.toInt - Trog.translationY
    true
  }

  override def touchDown(
                          screenX: Int,
                          screenY: Int,
                          pointer: Int,
                          button: Int
                        ): Boolean = {
    game.clicked = true
    true
  }

  override def touchUp(
                        screenX: Int,
                        screenY: Int,
                        pointer: Int,
                        button: Int
                      ): Boolean = {
    game.clicked = false
    true
  }
}

