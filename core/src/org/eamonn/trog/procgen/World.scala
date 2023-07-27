package org.eamonn.trog.procgen

import org.eamonn.trog.character.{Archetype, Archetypes}

case class World () {
  var archetypeList: List[Archetype] = Archetypes.getArchList
}
