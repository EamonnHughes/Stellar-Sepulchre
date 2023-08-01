package org.eamonn.trog

trait Actor extends Serializable {
  var location: Vec2
  var stats: Stats
  var equipment: Equipment
  var name: String
}
