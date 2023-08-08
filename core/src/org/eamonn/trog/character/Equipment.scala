package org.eamonn.trog.character

import org.eamonn.trog.items.Weapon

class Equipment extends Serializable {
  var weapon: Option[Weapon] = None
}