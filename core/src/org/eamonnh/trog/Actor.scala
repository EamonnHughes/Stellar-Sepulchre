package org.eamonnh.trog

import org.eamonnh.trog.character.{Equipment, Stats, Statuses}
import org.eamonnh.trog.items.{Item, Weapon}

trait Actor extends Serializable {
  var location: Vec2
  var destination: Vec2
  var statuses: Statuses = Statuses()
  var stats: Stats
  var equipment: Equipment
  var name: String

  def attack(target: Actor): Unit

  def equip(equ: Item): Unit = {
    equ match {
      case weapon: Weapon => {
        equipment.weapon.foreach(w => {
          w.onUnequip(this)
        })
        weapon.onEquip(this)
        equipment.weapon = Some(weapon)
      }
      case _ =>
    }
  }
}
