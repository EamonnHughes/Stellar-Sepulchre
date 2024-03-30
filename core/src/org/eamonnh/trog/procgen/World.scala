package org.eamonnh.trog.procgen

import org.eamonnh.trog.character.{Archetype, CaedanautArchetype, CondottiereArchetype}

import scala.util.Random

case class World() {
  var name: String = "world" + Random.nextInt(100).toString
  var archetypeList: List[Archetype] =
    List(CondottiereArchetype(), CaedanautArchetype())
}

case class WorldList(l: List[String]) {
  var list: List[String] = l
}
