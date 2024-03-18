package org.eamonnh.trog.character

import org.eamonnh.trog.items.makeCommonWeapon
import org.eamonnh.trog.scenes.Game

trait Archetype {
  val name: String
  val metaArchName: String

  def onSelect(game: Game): Unit

  def onLevelUp(game: Game): Unit
}

case class InfiltratorArchetype() extends Archetype {
  val name = "Infiltrator"
  val metaArchName: String = "Infiltrator"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += .5f
    game.player.stats.critChance += 2
    game.player.stats.critMod += .2f
    game.player.stats.skills = MicroMissile() :: game.player.stats.skills
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
      Charge() :: Bash() :: game.player.stats.skills
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
