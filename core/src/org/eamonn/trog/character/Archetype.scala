package org.eamonn.trog.character

import org.eamonn.trog.items.makeCommonWeapon
import org.eamonn.trog.scenes.Game

trait Archetype {
  val name: String
  def onSelect(game: Game): Unit
  def onLevelUp(game: Game): Unit
  val metaArchName: String
}

case class InfiltratorArchetype() extends Archetype {
  val name = "Infiltrator"
  val metaArchName: String = "Infiltrator"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += .5f
    game.player.stats.critChance += 2
    game.player.stats.critMod += .2f
    game.player.stats.skills = throwDagger() :: game.player.stats.skills
    val weapon = makeCommonWeapon(0, game, 1, 4, "Player")
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

case class CaedanautArchetype() extends Archetype {
  val name: String = "Caedanaut"

  val metaArchName: String = "Caedanaut"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1
    game.player.stats.damageMod += 1
    game.player.stats.skills =
      Dash() :: shieldBash() :: game.player.stats.skills
    val weapon = makeCommonWeapon(0, game, 1, 6, "Player")
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
