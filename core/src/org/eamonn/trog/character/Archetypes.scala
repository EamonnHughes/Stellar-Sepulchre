package org.eamonn.trog.character

import org.eamonn.trog.scenes.Game

import scala.util.Random

object Archetypes {
  var prefixes: List[prefix] = List(
    TankPre(),
    AccuracyPre()
  )
  var suffixes: List[suffix] = List(
    ExpSuf(),
    DamageSuf()
  )
  def createNewAchetype(): Archetype = {
    Archetype(
      prefixes(Random.nextInt(prefixes.length)),
      suffixes(Random.nextInt(suffixes.length))
    )
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

case class TankPre() extends prefix {
  val names: List[String] = List(
    "Ward",
    "Vital",
    "Shield"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.maxHealth += 5
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.maxHealth += 5
  }
}

case class AccuracyPre() extends prefix {
  val names: List[String] = List(
    "Chance",
    "Seer",
    "Scry"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1f
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.attackMod += .5f
    game.player.stats.critChance += 2
  }
}

case class ExpSuf() extends suffix {
  val names: List[String] = List(
    "scout"
  )

  override def onSelect(game: Game): Unit = {}

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.nextExp = (game.player.stats.nextExp*.9f).toInt
  }
}

case class DamageSuf() extends suffix {
  val names: List[String] = List(
    "knight",
    "breaker"
  )

  override def onSelect(game: Game): Unit = {
    game.player.stats.damageMod += 1
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.damageMod += 1
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
