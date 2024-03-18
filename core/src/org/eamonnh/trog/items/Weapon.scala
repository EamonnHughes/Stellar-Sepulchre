package org.eamonnh.trog.items

import org.eamonnh.trog.Trog.garbage
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.TextureWrapper
import org.eamonnh.trog.{Actor, Vec2, d}

import scala.util.Random


trait Weapon extends Gear {
  var mod: Int
  var weaponType: String

  def name: String = {
    if (mod > 0) {
      s"${weaponType} (+$mod)"
    } else if (mod < 0) {
      s"${weaponType} (-${Math.abs(mod)})"
    } else {
      s"${weaponType}"
    }
  }

  def onAttack(attacker: Actor, target: Actor): Unit
}

case class makeCommonWeapon(var mod: Int, var game: Game, nODie: Int, die: Int, field: String)
  extends Weapon {
  override var location: Option[Vec2] = None
  var weaponType: String = gearNames
    .getCINofD(nODie, die, field)(
      Random.nextInt(gearNames.getCINofD(nODie, die, field).length)
    )
    .substring(2)

  override def onAttack(attacker: Actor, target: Actor): Unit = {
    if (d(10) + attacker.stats.attackMod + mod > target.stats.ac) {
      var damage = (d(nODie, die) + attacker.stats.damageMod + mod)
      if (Random.nextInt(100) <= attacker.stats.critChance)
        damage = (attacker.stats.critMod * damage).toInt.toFloat
      target.stats.health -= damage.toInt
      if (target == game.player) game.player.lastStrike = s"a ${attacker.name}"
    }
  }

  override def onEquip(equipper: Actor): Unit = {
    game.addMessage("You equipped " + name)
  }

  override def onUnequip(equipper: Actor): Unit = {
    game.addMessage("You unequipped " + name)
  }

  override def groundTexture: TextureWrapper =
    TextureWrapper.load(s"$weaponType.png")

  override def use(actor: Actor): Unit = {
    actor.equip(this)
  }
}

object gearNames {
  val commonItemNames: List[String] =
    List(
      "14Dagger",
      "16Glaive"
    )
  val playerItemNames: List[String] =
    List(
      "14Dagger",
      "16Glaive"
    )

  val droneItemNames: List[String] = List(
    "11Baton",
    "12Baton",
    "13Baton",
    "14Baton",
    "15Baton",
    "16Baton"
  )

  def getCINofD(nODie: Int, die: Int, field: String): List[String] = {
    if (field == "Drone") {
      droneItemNames.filter(CIN =>
        CIN.charAt(0) == nODie.toString.charAt(0) && CIN.charAt(1) == die.toString
          .charAt(0)
      )
    } else if (field == "Player") {
      playerItemNames.filter(CIN =>
        CIN.charAt(0) == nODie.toString.charAt(0) && CIN.charAt(1) == die.toString
          .charAt(0)
      )
    } else {
      commonItemNames.filter(CIN =>
        CIN.charAt(0) == nODie.toString.charAt(0) && CIN.charAt(1) == die.toString
          .charAt(0)
      )
    }
  }
}
