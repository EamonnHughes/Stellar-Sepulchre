package org.eamonn.trog

import scala.util.Random

class Equipment extends Serializable {
  var weapon: Option[Weapon] = None

}

trait Item {
  var location: Option[Vec2]
  var possessor: Option[Actor]
}

trait Gear extends Item{
  def onEquip(equipper: Actor)
  def onUnequip(equipper: Actor)
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

case class Sword(var mod: Int) extends Weapon {
  override def onAttack(attacker: Actor, target: Actor): Unit = {
    if (d(10) + attacker.stats.attackMod + mod > target.stats.ac) {
      var damage = (d(6) + attacker.stats.damageMod + mod)
      if (Random.nextInt(100) <= attacker.stats.critChance) {
        damage = (attacker.stats.critMod * damage).toInt
      }
      target.stats.health -= damage.toInt
    }
  }

  override def onEquip(equipper: Actor): Unit = {}

  override def onUnequip(equipper: Actor): Unit = {}

  var weaponType: String = "Sword"
  override var location: Option[Vec2] = None
  override var possessor: Option[Actor] = None
}

case class Dagger(var mod: Int) extends Weapon {
  override def onAttack(attacker: Actor, target: Actor): Unit = {
    if (d(10) + attacker.stats.attackMod + mod > target.stats.ac) {
      var damage = (d(3) + attacker.stats.damageMod + mod)
      if (Random.nextInt(100) <= attacker.stats.critChance)
        damage = (attacker.stats.critMod * damage).toInt
      target.stats.health -= damage.toInt
    }
  }

  override def onEquip(equipper: Actor): Unit = {}

  override def onUnequip(equipper: Actor): Unit = {}
  var weaponType: String = "Dagger"
  override var location: Option[Vec2] = None
  override var possessor: Option[Actor] = None
}
