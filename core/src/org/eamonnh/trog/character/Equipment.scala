package org.eamonnh.trog.character

import org.eamonnh.trog.items.Weapon

class Equipment extends Serializable {
  var weapon: Option[Weapon] = None
}