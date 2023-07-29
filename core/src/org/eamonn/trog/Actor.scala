package org.eamonn.trog

trait Actor extends Serializable {
  var location: Vec2
  var stats: Stats
}
