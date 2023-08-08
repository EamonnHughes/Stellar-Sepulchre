package org.eamonn.trog.character

import org.eamonn.trog.scenes.Game

trait prefix {
  val names: List[String]
  def onSelect(game: Game): Unit
  def onLevelUp(game: Game): Unit
}

case class HealthPrefix() extends prefix {
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
case class ArmorPrefix() extends prefix {
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

case class AccuracyPrefix() extends prefix {
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
