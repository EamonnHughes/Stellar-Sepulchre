package org.eamonnh.trog

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.Animation

object inGameUserInterface {
  var inInventory = false
  var inCharacterSheet = false

  def renderUI(batch: PolygonSpriteBatch, game: Game): Unit = {
    val player = game.player
    val floor = game.floor
    if (!inInventory && !inCharacterSheet && !game.player.exploring)
      drawCursorUI(batch, game)
    if (player.stats.health <= (player.stats.maxHealth * .3f)) {
      batch.setColor(1f, 0f, 0f, .2f)
      batch.draw(
        Trog.EffectSplash,
        -Trog.translationX * screenUnit,
        -Trog.translationY * screenUnit,
        Geometry.ScreenWidth,
        Geometry.ScreenHeight
      )
    }
    batch.setColor(1f, .75f, 0f, game.lvlupEffect)
    batch.draw(
      Trog.EffectSplash,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      Geometry.ScreenWidth,
      Geometry.ScreenHeight
    )

    batch.setColor(Color.WHITE)
    batch.draw(
      Trog.UICornerLeft,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      screenUnit * 4,
      screenUnit * 4
    )
    batch.draw(
      Trog.UICornerRight,
      -Trog.translationX * screenUnit + Geometry.ScreenWidth - (screenUnit * 4),
      -Trog.translationY * screenUnit,
      screenUnit * 4,
      screenUnit * 4
    )
    batch.draw(
      Trog.UIHotbar,
      -Trog.translationX * screenUnit + 4 * screenUnit,
      -Trog.translationY * screenUnit,
      screenUnit * 12,
      screenUnit * 4
    )
    batch.setColor(Color.FIREBRICK)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      screenUnit * 4,
      screenUnit / 2
    )
    batch.setColor(Color.RED)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      screenUnit * 4 * player.stats.health / player.stats.maxHealth,
      screenUnit / 2
    )
    batch.setColor(Color.YELLOW)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + screenUnit / 2,
      screenUnit * 4,
      screenUnit / 4
    )
    batch.setColor(Color.ORANGE)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + screenUnit / 2,
      screenUnit * 4 * player.stats.exp / player.stats.nextExp,
      screenUnit / 4
    )
    batch.setColor(Color.WHITE)
    batch.draw(
      Trog.UIXPBarFrame,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit + screenUnit / 2,
      screenUnit * 4,
      screenUnit / 4
    )
    batch.draw(
      Trog.UIHealthBarFrame,
      -Trog.translationX * screenUnit,
      -Trog.translationY * screenUnit,
      screenUnit * 4,
      screenUnit / 2
    )
    Text.tinyFont.draw(
      batch,
      s"${player.stats.health}/${player.stats.maxHealth}",
      -Trog.translationX * screenUnit + (.1f * screenUnit),
      -Trog.translationY * screenUnit + (.35f * screenUnit)
    )
    player.stats.skills.zipWithIndex.foreach({
      case (s, i) => {
        batch.draw(
          s.icon,
          -Trog.translationX * screenUnit + (((i * 2f) + 6) * screenUnit),
          -Trog.translationY * screenUnit,
          screenUnit * 2f,
          screenUnit * 2f
        )
        Text.smallFont.draw(
          batch,
          s"${i + 1}",
          -Trog.translationX * screenUnit + (((i * 2f) + 6) * screenUnit),
          -Trog.translationY * screenUnit
        )
        if (s.ccd > 0) {
          Text.hugeFont.setColor(.3f, .3f, .3f, .8f)
          Text.hugeFont.draw(
            batch,
                s"${s.ccd.toString}",
            -Trog.translationX * screenUnit + (((i * 2f) + 6) * screenUnit + (screenUnit * .4f)),
            -Trog.translationY * screenUnit + (screenUnit * 2f) - (screenUnit * .4f)
          )
        }
      }
    })
    drawConsole(batch, game)
    if (inCharacterSheet) {
      drawCharacterSheet(batch, game)
    } else if (inInventory) {
      drawInventory(batch, game)
    } else if (player.inPerkChoice) {
      drawPerkChoice(batch, game)
    }

  }

  def drawCursorUI(batch: PolygonSpriteBatch, game: Game): Unit = {
    var loc = game.mouseLocOnGrid

    var path = Pathfinding.findPath(game.player.location, loc, game.level)
    if (path.isEmpty || !game.explored.contains(loc)) {
      batch.setColor(1f, 0f, 0f, .75f)
      Animation.twoFrameAnimation(
        game,
        batch,
        "mouseHover",
        loc.x.toFloat,
        loc.y.toFloat
      )
    } else {
      path.foreach(p => {
        batch.setColor(1f, 1f, 1f, .75f)
        Animation.twoFrameAnimation(
          game,
          batch,
          "mouseHover",
          loc.x.toFloat,
          loc.y.toFloat
        )
        p.list.reverse.tail.foreach(l => {
          batch.setColor(1f, 1f, 1f, .75f)
          Animation.twoFrameAnimation(
            game,
            batch,
            "pathTrail",
            l.x.toFloat,
            l.y.toFloat
          )
        })
      })
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
      ((-Trog.translationY + 6) * screenUnit)
    )

  }
  def drawCharacterSheet(batch: PolygonSpriteBatch, game: Game): Unit = {
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
      game.player.equipment.weapon.foreach(w => str = w.name)
      str
    }

    Text.mediumFont.draw(
      batch,
      s" " +
        s"${game.player.name}, the level ${game.player.stats.level} ${game.player.archetype.name}\n " +
        s"Experience: ${game.player.stats.exp}/${game.player.stats.nextExp}\n " +
        s"Floor: ${game.floor}\n " +
        s"Health: ${game.player.stats.health}/${game.player.stats.maxHealth}\n " +
        s"Armor Class: ${game.player.stats.ac}\n " +
        s"Sight Radius: ${game.player.stats.sightRad}\n " +
        s"Attack Bonus: ${game.player.stats.attackMod}\n " +
        s"Damage Bonus: ${game.player.stats.damageMod}\n " +
        s"Crit Modifier: %${game.player.stats.critMod * 100}\n " +
        s"Crit Chance: %${game.player.stats.critChance}\n\n " +
        s"Helm: ${uiHelmName}\n " +
        s"Body Armor: ${uiArmorName}\n " +
        s"Weapon: ${uiWeaponName}\n " +
        s"Perks:\n ${game.player.perks.map(_.title).sorted.mkString("\n ")}",
      -Trog.translationX * screenUnit + (2 * screenUnit),
      (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
    )
  }
  def drawInventory(batch: PolygonSpriteBatch, game: Game): Unit = {
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
      .filter(i => i.possessor.nonEmpty && i.possessor.head == game.player)
      .filter(n => n.tNum >= 1)
      .zipWithIndex
      .foreach({
        case (item, index) => {
          if (index == game.player.menuItemSelected) {
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

  def drawPerkChoice(batch: PolygonSpriteBatch, game: Game): Unit = {
    batch.setColor(0f, 0f, 0f, .5f)
    batch.draw(
      Trog.Square,
      -Trog.translationX * screenUnit + (2 * screenUnit),
      -Trog.translationY * screenUnit + (2 * screenUnit),
      (Geometry.ScreenWidth - (4 * screenUnit)),
      (Geometry.ScreenHeight - (4 * screenUnit))
    )
    var perks: String = ""
    game.player.perkChoices
      .zipWithIndex
      .foreach({
        case (perk, index) => {
          if (index == game.player.menuItemSelected) {
            perks = s"$perks \n - ${perk.title}"
          } else {
            perks = s"$perks \n ${perk.title}"
          }
        }
      })
    Text.mediumFont.draw(
      batch,
      s"Select a perk:\n$perks\n\n${game.player.perkChoices(game.player.menuItemSelected).description}",
      -Trog.translationX * screenUnit + (2 * screenUnit),
      (-Trog.translationY * screenUnit) + Geometry.ScreenHeight - (2 * screenUnit)
    )
  }
}
