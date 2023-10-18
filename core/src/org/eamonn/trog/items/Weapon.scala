package org.eamonn.trog.items

import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.{Actor, Vec2, d}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random


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
case class makeCommonWeapon(var mod: Int, var game: Game, nODie: Int, die: Int)
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

  override def onEquip(equipper: Actor): Unit = {
    game.addMessage("You equipped " + name)
  }

  override def onUnequip(equipper: Actor): Unit = {
    game.addMessage("You unequipped " + name)
  }
  var weaponType: String = gearNames
    .getCINofD(nODie, die)(
      Random.nextInt(gearNames.getCINofD(nODie, die).length)
    )
    .substring(2)
  override var location: Option[Vec2] = None

  override def groundTexture: TextureWrapper =
    TextureWrapper.load(s"$weaponType.png")

  override def use(actor: Actor): Unit = {
    actor.equip(this)
  }
}

object gearNames {
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
