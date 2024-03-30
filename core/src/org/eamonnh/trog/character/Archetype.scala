package org.eamonnh.trog.character

import org.eamonnh.trog.items.makeCommonWeapon
import org.eamonnh.trog.scenes.Game

trait Archetype {
  def name: String
  def metaArchName: String
  def description: String

  def onSelect(game: Game): Unit

  def onLevelUp(game: Game): Unit
}

case class CondottiereArchetype() extends Archetype {
  def name = "Condottiere"
  def metaArchName: String = "Condottiere"

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

  def description: String = "An expert in both intrigue and violence, the Condottiere has achieved mastery\n" +
      "of the blade and the bullet. As well as 20 billion dead, the Centaurine Civil\n" +
      "War created many opportunities for a man with the Condottiere's talents, and,\n" +
      "not one to refuse honest work, he signed on as a mercenary for the Consortium."
}

case class CaedanautArchetype() extends Archetype {
  def name: String = "Caedanaut"

  def metaArchName: String = "Caedanaut"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1
    game.player.stats.damageMod += 1
    game.player.stats.skills = Charge() :: Bash() :: game.player.stats.skills
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

  def description: String = {
      "Cowardice was unacceptable to the Lacedaemonian Guard, and, when the Caedanaut fled\n" +
      "the field, he was given the choice: crucifixion or being sold to the Consortium.\n" +
      "Choosing the latter, he worked aboard a ship for years. At the onset of the Civil War\n" +
      "his prowess in battle was noticed, and he was offered a chance to redeem himself."
  }
}
