package org.eamonn.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.scenes.Game

object inGameUserInterface {
  var inInventory = false
  var inCharacterSheet = false
  def renderUI(batch: PolygonSpriteBatch, game: Game): Unit = {
    val player = game.player
    val floor = game.floor
    batch.setColor(Color.WHITE)
    player.stats.skills.zipWithIndex.foreach({
      case (s, i) => {
        batch.draw(
          s.icon,
          -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
          -Trog.translationY * screenUnit,
          screenUnit * 1.5f,
          screenUnit * 1.5f
        )
        Text.smallFont.draw(
          batch,
          s"${i + 1}",
          -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
          -Trog.translationY * screenUnit + (screenUnit * 1.83f)
        )
        if (s.ccd > 0) {
          Text.hugeFont.setColor(1f, 1f, 1f, 0.5f)
          Text.hugeFont.draw(
            batch,
            s"${s.ccd.toString}",
            -Trog.translationX * screenUnit + (((i * 1.5f) + 6) * screenUnit),
            -Trog.translationY * screenUnit + (screenUnit * 1.5f)
          )
        }
      }
    })
    Text.mediumFont.draw(
      batch,
      s"${player.name}, the level ${player.stats.level} ${player.archetype.name} on floor ${floor}",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight
    )
    batch.setColor(Color.YELLOW)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4,
      screenUnit / 8
    )
    batch.setColor(Color.ORANGE)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit),
      screenUnit * 4 * player.stats.exp / player.stats.nextExp,
      screenUnit / 8
    )
    batch.setColor(Color.FIREBRICK)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4,
      screenUnit / 2
    )
    batch.setColor(Color.RED)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - (screenUnit * 3 / 2),
      screenUnit * 4 * player.stats.health / player.stats.maxHealth,
      screenUnit / 2
    )
    batch.setColor(Color.WHITE)
    Text.mediumFont.draw(
      batch,
      s"${player.stats.health}/${player.stats.maxHealth}",
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + Geometry.ScreenHeight - screenUnit
    )
    drawConsole(batch, game)
    if (inCharacterSheet) {
      batch.setColor(0f, 0f, 0f, .5f)
      batch.draw(
        Trog.Square,
        -Trog.translationX * screenUnit + (2 * screenUnit),
        -Trog.translationY * screenUnit + (2 * screenUnit),
        (Geometry.ScreenWidth - (4 * screenUnit)),
        (Geometry.ScreenHeight - (4 * screenUnit))
      )
      Text.mediumFont.setColor(Color.WHITE)

      def uiHelmName: String = {
        "None"
      }

      def uiArmorName: String = {
        "None"
      }

      def uiWeaponName: String = {
        var str = "None"
        player.equipment.weapon.foreach(w => str = w.name)
        str
      }

      Text.mediumFont.draw(
        batch,
        s" " +
          s"Name: ${player.name}\n " +
          s"Archetype: ${player.archetype.name}\n " +
          s"Level: ${player.stats.level}\n " +
          s"Experience: ${player.stats.exp}/${player.stats.nextExp}\n " +
          s"Floor: $floor\n " +
          s"Health: ${player.stats.health}/${player.stats.maxHealth}\n " +
          s"Armor Class: ${player.stats.ac}\n " +
          s"Sight Radius: ${player.stats.sightRad}\n " +
          s"Attack Bonus: ${player.stats.attackMod}\n " +
          s"Damage Bonus: ${player.stats.damageMod}\n " +
          s"Crit Modifier: %${player.stats.critMod * 100}\n " +
          s"Crit Chance: %${player.stats.critChance}\n\n " +
          s"Helm: ${uiHelmName}\n " +
          s"Body Armor: ${uiArmorName}\n " +
          s"Weapon: ${uiWeaponName}",
        -Trog.translationX * screenUnit + (2 * screenUnit),
        (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
      )
    } else if (inInventory) {
      batch.setColor(0f, 0f, 0f, .5f)
      batch.draw(
        Trog.Square,
        -Trog.translationX * screenUnit + (2 * screenUnit),
        -Trog.translationY * screenUnit + (2 * screenUnit),
        (Geometry.ScreenWidth - (4 * screenUnit)),
        (Geometry.ScreenHeight - (4 * screenUnit))
      )
      var inv: String = ""
      game.items
        .filter(i => i.possessor.nonEmpty && i.possessor.head == player)
        .filter(n => n.tNum >= 1)
        .zipWithIndex
        .foreach({
          case (item, index) => {
            if (index == player.inventoryItemSelected) {
              inv = s"$inv \n - x${item.tNum} ${item.name}"
            } else {
              inv = s"$inv \n x${item.tNum} ${item.name}"
            }
          }
        })
      Text.mediumFont.draw(
        batch,
        s"INVENTORY:\n$inv",
        -Trog.translationX * screenUnit + (2 * screenUnit),
        (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
      )
    }
  }

  def drawConsole(batch: PolygonSpriteBatch, game: Game): Unit = {
    val messages = game.messages
    var log = messages
      .take(5)
      .reverse
      .mkString("\n")

    Text.smallFont.setColor(Color.WHITE)
    Text.smallFont.draw(
      batch,
      log,
      (-Trog.translationX * screenUnit),
      ((-Trog.translationY + 2) * screenUnit)
    )

  }
}
