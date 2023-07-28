package org.eamonn.trog.character

import org.eamonn.trog.scenes.Game

import scala.util.Random

object Archetypes {
  var prefixes: List[prefix] = List(
    HealthPre(),
    AccuracyPre(),
    ArmorPre(),
    CritPre()
  )
  var suffixes: List[suffix] = List(
    ExpSuf(),
    DamageSuf(),
    CritModSuf(),
    HealSuf()
  )
  def getArchList: List[Archetype] = {
    var archeList = List.empty[Archetype]
    var preL = prefixes
    var sufL = suffixes
    while(sufL.nonEmpty){
      var pre = preL(Random.nextInt(preL.length))
      var suf = sufL(Random.nextInt(sufL.length))
      preL = preL.filterNot(p => p eq pre)
      sufL = sufL.filterNot(s => s eq suf)
      archeList = Archetype(pre, suf) :: archeList
    }
    archeList
  }

}

trait prefix {
  val names: List[String]
  def onSelect(game: Game): Unit
  def onLevelUp(game: Game): Unit
}

trait suffix {
  val names: List[String]
  def onSelect(game: Game): Unit
  def onLevelUp(game: Game): Unit
}

case class HealthPre() extends prefix {
  val names: List[String] = List(
    "Vital",
    "Vim"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.maxHealth += 5
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.maxHealth += 5
  }
}
case class ArmorPre() extends prefix {
  val names: List[String] = List(
    "Ward",
    "Shield"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.ac += 1
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.ac += 1
  }
}

case class AccuracyPre() extends prefix {
  val names: List[String] = List(
    "Seer",
    "Scry"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1f
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.attackMod += .5f
  }
}
case class CritPre() extends prefix {
  val names: List[String] = List(
    "Chance",
    "Luck"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.critChance += 2
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.critChance += 1
  }
}
case class ExpSuf() extends suffix {
  val names: List[String] = List(
    "scout"
  )

  override def onSelect(game: Game): Unit = {}

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.nextExp = (game.player.stats.nextExp * .9f).toInt
  }
}

case class DamageSuf() extends suffix {
  val names: List[String] = List(
    "knight",
    "breaker",
    "blade"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.damageMod += 1
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.damageMod += 1
  }
}
case class CritModSuf() extends suffix {
  val names: List[String] = List(
    "ranger",
    "seeker"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.critMod += .2f
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.critMod += .2f
  }
}
case class HealSuf() extends suffix {
  val names: List[String] = List(
    "venerator",
    "paladin"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.health += 3
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.health += 3
  }
}

case class Archetype(prefix: prefix, suffix: suffix) {
  val name: String =
    prefix.names(Random.nextInt(prefix.names.length)) + suffix.names(
      Random.nextInt(suffix.names.length)
    )
  def onSelect(game: Game): Unit = {
    prefix.onSelect(game)
    suffix.onSelect(game)
  }

  def onLevelUp(game: Game): Unit = {
    prefix.onLevelUp(game)
    suffix.onLevelUp(game)
  }
}
