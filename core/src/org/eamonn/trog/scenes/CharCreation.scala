package org.eamonn.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.character.{Archetype, Archetypes}
import org.eamonn.trog.procgen.{GeneratedMap, Level, World}

import java.awt.RenderingHints.Key

class CharCreation(world: World) extends Scene {
  var arches: List[Archetype] = world.archetypeList
  var player: Player = Player()
  var ready = false
  var selectedArch = 0
  override def init(): InputAdapter = {
    new CharCreationControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    if (ready) player.archetype = arches(selectedArch)
    if (ready)
      Some(new LevelGen(player, None, world))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s" Select an Archetype:",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
    )
    arches.zipWithIndex.foreach({ case (a, i) =>
      if (selectedArch == i) Text.mediumFont.setColor(Color.WHITE)
      else Text.mediumFont.setColor(Color.LIGHT_GRAY)
      Text.mediumFont.draw(
        batch,
        s"[${i + 1}] ${a.name}",
        -Trog.translationX * screenUnit,
        (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight / 2) - (screenUnit * (i + 1))
      )
    })

  }
}

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
    if (keycode == Keys.ENTER) {
      creation.ready = true
    }
    true
  }

  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Keys.DOWN) {
      creation.selectedArch =
        (creation.selectedArch + 1) % creation.arches.length
    } else if (keycode == Keys.UP) {
      creation.selectedArch =
        (creation.selectedArch + creation.arches.length - 1) % creation.arches.length
    }
    true
  }
}
