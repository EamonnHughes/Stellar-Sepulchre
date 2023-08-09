package org.eamonn.trog.character

import org.eamonn.trog.items.makeCommonWeapon
import org.eamonn.trog.scenes.Game

trait Suffix {
  val names: List[String]
  def onSelect(game: Game): Unit
  def onLevelUp(game: Game): Unit
  val metaArchName: String
}

case class RogueSuffix() extends Suffix {
  val names: List[String] = List(
    "blade", "thief", "taker", "trickster"
  )
  val metaArchName: String = "Rogue"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += .5f
    game.player.stats.critChance += 2
    game.player.stats.critMod += .2f
    game.player.stats.skills = throwDagger() :: game.player.stats.skills
    val weapon = makeCommonWeapon(0, game, 1, 4)
    weapon.possessor = Some(game.player)
    weapon.game = game
    game.items = weapon :: game.items
    game.player.equipment.weapon = Some(weapon)
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.attackMod += .25f
    game.player.stats.critChance += 1
    game.player.stats.critMod += .1f
  }
}

case class FighterSuffix() extends Suffix {
  val names: List[String] = List(
    "knight", "paladin", "breaker", "sword"
  )
  val metaArchName: String = "Fighter"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1
    game.player.stats.damageMod += 1
    val weapon = makeCommonWeapon(0, game, 1, 6)
    weapon.possessor = Some(game.player)
    weapon.game = game
    game.items = weapon :: game.items
    game.player.equipment.weapon = Some(weapon)
  }

  override def onLevelUp(game: Game): Unit = {
    game.player.stats.attackMod += .5f
    game.player.stats.damageMod += .5f
  }
}
