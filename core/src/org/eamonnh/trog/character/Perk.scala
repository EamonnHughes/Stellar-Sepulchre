package org.eamonnh.trog.character

trait Perk {
def isAllowed(player: Player): Boolean
  def onApply(player: Player): Unit
  def title: String
  def description: String
}

case class Ironman1() extends Perk {

  override def isAllowed(player: Player): Boolean = {
    true
  }

  override def onApply(player: Player): Unit = {
    player.stats.maxHealth += 5
    player.stats.health += 5
  }

  override def title: String = "Ironman I"

  override def description: String = "Increases max health by 5"
}
case class Ironman2() extends Perk {

  override def isAllowed(player: Player): Boolean = {
    if(player.perks.contains(Ironman1)) true else false
  }

  override def onApply(player: Player): Unit = {
    player.stats.maxHealth += 5
    player.stats.health += 5
  }

  override def title: String = "Ironman II"

  override def description: String = "Increases max health by 5"
}
case class Ironman3() extends Perk {

  override def isAllowed(player: Player): Boolean = {
    if(player.perks.contains(Ironman2)) true else false
  }

  override def onApply(player: Player): Unit = {
    player.stats.maxHealth += 5
    player.stats.health += 5
  }

  override def title: String = "Ironman III"

  override def description: String = "Increases max health by 5"
}

case class Scout() extends Perk {

  override def isAllowed(player: Player): Boolean = {
    true
  }

  override def onApply(player: Player): Unit = {
    player.stats.sightRad += 1
  }

  override def title: String = "Scout"

  override def description: String = "Increases sight range by 1"
}
case class Surgeon() extends Perk {

  override def isAllowed(player: Player): Boolean = {
    true
  }

  override def onApply(player: Player): Unit = {
    player.stats.critChance += 5
  }

  override def title: String = "Surgeon"

  override def description: String = "Increases crit chance by 5%"
}