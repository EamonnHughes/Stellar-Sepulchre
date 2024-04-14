package org.eamonnh.trog.character

import org.eamonnh.trog.items.{PlayerGiver, makeCommonWeapon}
import org.eamonnh.trog.scenes.Game

trait Archetype {
  def name: String
  def metaArchName: String
  def description: String
  def logo: String

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
    game.player.stats.skills =
      MicroMissile() :: Disengage() :: Lacerate() :: game.player.stats.skills
    game.player.perkPool = Ironman1() :: Ironman2() :: Ironman3() :: Scout() :: Surgeon() :: game.player.perkPool
    val weapon = makeCommonWeapon(0, game, 1, 4, PlayerGiver())
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

  def description: String =
    "An expert in both intrigue and violence, \nthe Condottiere has achieved mastery\n" +
      "of the blade and the bullet. As well as \n20 billion dead, the Centaurine Civil\n" +
      "War created many opportunities for a man \nwith the Condottiere's talents, and,\n" +
      "not one to refuse honest work, he signed \non as a mercenary for the Consortium."

  override def logo: String = "Condottiere-Logo.png"
}

case class CaedanautArchetype() extends Archetype {
  def name: String = "Caedanaut"
  override def logo: String = "Caedanaut-Logo.png"


  def metaArchName: String = "Caedanaut"

  override def onSelect(game: Game): Unit = {
    game.player.stats.attackMod += 1
    game.player.stats.damageMod += 1
    game.player.stats.skills = Charge() :: Bash() :: game.player.stats.skills
    game.player.perkPool = Ironman1() :: Ironman2() :: Ironman3() :: Scout() :: Surgeon() :: game.player.perkPool
    val weapon = makeCommonWeapon(0, game, 1, 6, PlayerGiver())
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
    "Cowardice was unacceptable to the \nLacedaemonian Guard, and, when the Caedanaut\n" +
      "fled the field, he was given the choice: \ncrucifixion or being sold to the Consortium.\n" +
      "Choosing the latter, he worked aboard a \nship for years. At the onset of the Civil War\n" +
      "his prowess in battle was noticed, and he \nwas offered a chance to redeem himself."
  }
}
