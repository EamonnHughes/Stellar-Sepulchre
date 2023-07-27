package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.character.{Archetype, Archetypes}
import org.eamonn.trog.procgen.{GeneratedMap, Level}

class CharCreation extends Scene {
  var player: Player = Player()
  var optionOne: Archetype = Archetypes.createNewAchetype()
  var optionTwo: Archetype = Archetypes.createNewAchetype()
  var optionThree: Archetype = Archetypes.createNewAchetype()
  var ready = false
  override def init(): InputAdapter = {
    new CharCreationControl(this)
  }
  override def update(delta: Float): Option[Scene] = {

    if (ready) Some(new LevelGen(player, None)) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s" Select an Archetype\n [1] ${optionOne.name}\n [2] ${optionTwo.name}\n [3] ${optionThree.name}",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
  }
}

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter

class CharCreationControl(creation: CharCreation) extends InputAdapter {
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
    if (keycode == Keys.NUM_1) {
      creation.player.archetype = creation.optionOne
      creation.ready = true
    } else if (keycode == Keys.NUM_2) {
      creation.player.archetype = creation.optionTwo
      creation.ready = true
    } else if (keycode == Keys.NUM_3) {
      creation.player.archetype = creation.optionThree
      creation.ready = true
    }

    true
  }
}
