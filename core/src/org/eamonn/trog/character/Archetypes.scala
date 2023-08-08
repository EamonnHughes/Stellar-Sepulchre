package org.eamonn.trog.character

import org.eamonn.trog.scenes.Game

import scala.util.Random

object Archetypes {
  var prefixes: List[prefix] = List(
    HealthPrefix(),
    AccuracyPrefix(),
    ArmorPrefix()
  )
  var suffixes: List[Suffix] = List(
    RogueSuffix(),
    FighterSuffix()
  )
  def getArchList: List[Archetype] = {
    var archeList = List.empty[Archetype]
    var preL = prefixes
    var sufL = suffixes
    while (sufL.nonEmpty) {
      var pre = preL(Random.nextInt(preL.length))
      var suf = sufL(Random.nextInt(sufL.length))
      preL = preL.filterNot(p => p eq pre)
      sufL = sufL.filterNot(s => s eq suf)
      archeList = Archetype(pre, suf) :: archeList
    }
    archeList
  }

}
case class Archetype(prefix: prefix, suffix: Suffix) {
  val name: String = {
    prefix.names(Random.nextInt(prefix.names.length)) + suffix.names(
      Random.nextInt(suffix.names.length)
    )
  }
  val metaArchName: String = suffix.metaArchName
  def onSelect(game: Game): Unit = {
    prefix.onSelect(game)
    suffix.onSelect(game)
  }

  def onLevelUp(game: Game): Unit = {
    prefix.onLevelUp(game)
    suffix.onLevelUp(game)
  }
}
