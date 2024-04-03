package org.eamonnh.trog
package scenes

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Scene
import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.character.{Archetype, Player}
import org.eamonnh.trog.procgen.World
import org.eamonnh.trog.util.TextureWrapper

class CharCreation(world: World) extends Scene {
  var arches: List[Archetype] = world.archetypeList
  var player: Player = Player()
  var name = ""
  var selectedArch = 0
  var entered = false

  override def init(): InputAdapter = {
    new CharCreationControl(this)
  }

  override def update(delta: Float): Option[Scene] = {
    if (entered && (player.archetype eq null)) {
      player.archetype = arches(selectedArch)
      entered = false
    }
    if (name != "" && entered && (player.archetype ne null)) {
      player.name = name
      entered = false
    } else {
      entered = false
    }
    if (!(player.archetype eq null) && player.name != "")
      Some(new LevelGen(player, None, world))
    else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {}

  override def renderUI(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    if (player.archetype eq null) {
      Text.mediumFont.draw(
        batch,
        s" Select an Archetype:",
        -Trog.translationX * screenUnit,
        -Trog.translationY * screenUnit + Geometry.ScreenHeight * .75f
      )
      batch.setColor(Color.WHITE)
      batch.draw(TextureWrapper.load( arches(selectedArch).logo),-Trog.translationX*screenUnit + screenUnit, (-Trog.translationY * screenUnit) +  (Geometry.ScreenHeight * .75f) - (screenUnit * (arches.length + 3)) - (screenUnit*3), screenUnit*3, screenUnit*3)
      Text.smallFont.setColor(Color.WHITE)
      Text.smallFont.draw(
        batch,
        s"${arches(selectedArch).description}",
        -Trog.translationX * screenUnit + screenUnit * 5,
        (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight * .75f) - (screenUnit * (arches.length + 3))
      )

      arches.zipWithIndex.foreach({ case (a, i) =>
        if (selectedArch == i) Text.mediumFont.setColor(Color.WHITE)
        else Text.mediumFont.setColor(Color.LIGHT_GRAY)
        Text.mediumFont.draw(
          batch,
          s"[${i + 1}] ${a.name}",
          -Trog.translationX * screenUnit,
          (-Trog.translationY * screenUnit) + (Geometry.ScreenHeight * .75f) - (screenUnit * (i + 1))
        )
      })

    } else {
      Text.mediumFont.draw(
        batch,
        s" Enter Your Name: $name",
        -Trog.translationX * screenUnit,
        -Trog.translationY * screenUnit + Geometry.ScreenHeight / 2
      )
    }

  }

  override def updateCamera(): Unit = {}
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
      creation.entered = true
    }
    if (!(creation.player.name eq null)) {
      if (keycode == Keys.BACKSPACE) {
        creation.name = creation.name.dropRight(1)
      } else if (keycode == Keys.SPACE && creation.name.length < 20) {
        creation.name = creation.name + " "
      } else if (
        Keys.toString(keycode).length == 1 && creation.name.length < 20
      ) {
        creation.name = creation.name + Keys.toString(keycode).toLowerCase
      }
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
