package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

class Equipment extends Serializable {
  var weapon: Option[Weapon] = None
}

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

trait Usable extends Item {}

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

trait Weapon extends Gear {
  def name: String = {
    if (mod > 0) {
      s"${weaponType} (+$mod)"
    } else if (mod < 0) {
      s"${weaponType} (-${Math.abs(mod)})"
    } else {
      s"${weaponType}"
    }
  }
  var mod: Int
  var weaponType: String
  def onAttack(attacker: Actor, target: Actor)
}
case class makeCommonItem(var mod: Int, var game: Game, nODie: Int, die: Int)
    extends Weapon {
  override def onAttack(attacker: Actor, target: Actor): Unit = {
    if (d(10) + attacker.stats.attackMod + mod > target.stats.ac) {
      var damage = (d(nODie, die) + attacker.stats.damageMod + mod)
      if (Random.nextInt(100) <= attacker.stats.critChance)
        damage = (attacker.stats.critMod * damage).toInt
      target.stats.health -= damage.toInt
      if (target == game.player) game.player.lastStrike = s"a ${attacker.name}"
    }
  }

  override def onEquip(equipper: Actor): Unit = {}

  override def onUnequip(equipper: Actor): Unit = {}
  var weaponType: String = itemNames
    .getCINofD(nODie, die)(
      Random.nextInt(itemNames.getCINofD(nODie, die).length)
    )
    .substring(2)
  override var location: Option[Vec2] = None
  override var possessor: Option[Actor] = None

  override def groundTexture: TextureWrapper =
    TextureWrapper.load(s"$weaponType.png")

  override def use(actor: Actor): Unit = {
    actor.equip(this)
  }
}

object itemNames {
  val commonItemNames: List[String] =
    List(
      "12Club",
      "13Knuckledusters",
      "14Dagger",
      "15Shortsword",
      "16Longsword"
    )
  def getCINofD(nODie: Int, die: Int): List[String] = {
    commonItemNames.filter(CIN =>
      CIN.charAt(0) == nODie.toString.charAt(0) && CIN.charAt(1) == die.toString
        .charAt(0)
    )
  }
}
