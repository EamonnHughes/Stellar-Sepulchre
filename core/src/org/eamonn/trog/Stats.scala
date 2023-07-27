package org.eamonn.trog
case class Stats() {
  var ac = 5
  var exp = 0
  var nextExp = 50
  var maxHealth = 10
  var health: Int = maxHealth
  var sightRad = 6
  var healing = 0
  var level = 1
  var damageMod = 0
  var attackMod = 0f
  var critChance = 5
  var critMod = 2f
}
