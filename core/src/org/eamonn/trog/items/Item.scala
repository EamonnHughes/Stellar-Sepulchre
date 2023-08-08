package org.eamonn.trog.items

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.{Actor, Vec2, screenUnit}
import org.eamonn.trog.util.TextureWrapper

trait Item {
  var game: Game
  def name: String
  def groundTexture: TextureWrapper
  var location: Option[Vec2]
  def use(actor: Actor): Unit
  var possessor: Option[Actor]
  var number: Int = 1
  def tNum: Int
  def pickUp(actor: Actor): Unit = {
    var l =
      game.items.filter(i => i.possessor.nonEmpty && i.possessor.head == actor)
    l.foreach(i => {
      if (i.name == this.name && i.number < 99) {
        game.items = game.items.filterNot(it => it eq this)
        i.number += 1
      } else {
        location = None
        possessor = Some(actor)
      }
    })
    if (l.isEmpty) {
      location = None
      possessor = Some(actor)
    }
  }
  def draw(batch: PolygonSpriteBatch): Unit = {
    location.foreach(l => {
      batch.setColor(Color.WHITE)
      batch.draw(
        groundTexture,
        l.x * screenUnit,
        l.y * screenUnit,
        screenUnit,
        screenUnit
      )
    })
  }
}

trait Usable extends Item {
}

trait Gear extends Item {
  def onEquip(equipper: Actor)
  def onUnequip(equipper: Actor)
  override def tNum: Int = {
    var n = number
    if (possessor.forall(p => p.equipment.weapon.contains(this))) {
      n -= 1
    }

    n
  }
}

