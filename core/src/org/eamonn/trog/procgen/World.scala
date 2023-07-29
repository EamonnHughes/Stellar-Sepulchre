package org.eamonn.trog.procgen

import org.eamonn.trog.character.{Archetype, Archetypes}

import scala.util.Random

case class World () {
  var name: String = "world" + Random.nextInt(100).toString
  var archetypeList: List[Archetype] = Archetypes.getArchList
}

case class WorldList(l: List[String]) {
  var list: List[String] = l
}