package org.eamonnh.trog

import org.eamonnh.trog.character.{Equipment, Stats, Status}
import org.eamonnh.trog.items.{Item, Weapon}

trait Actor extends Serializable {
  var location: Vec2
  var destination: Vec2
  var statuses: List[Status] = List.empty
  var stats: Stats
  var equipment: Equipment
  var name: String
  var turn: Boolean

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
