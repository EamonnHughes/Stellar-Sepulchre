package org.eamonn.trog

trait Actor extends Serializable {
  var location: Vec2
  var stats: Stats
  var equipment: Equipment
  var name: String
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
