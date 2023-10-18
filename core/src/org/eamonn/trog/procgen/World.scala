package org.eamonn.trog.procgen

import org.eamonn.trog.character.{Archetype, FighterArchetype, RogueArchetype}

import scala.util.Random

case class World () {
  var name: String = "world" + Random.nextInt(100).toString
  var archetypeList: List[Archetype] = List(RogueArchetype(), FighterArchetype())
}

case class WorldList(l: List[String]) {
  var list: List[String] = l
}