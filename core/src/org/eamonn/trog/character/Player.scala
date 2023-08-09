package org.eamonn.trog.character

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.items.{HealingPotion, makeCommonWeapon}
import org.eamonn.trog.scenes.Game
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Actor, Enemy, Pathfinding, Vec2, d, screenUnit}

case class Player() extends Actor {
  var inventoryItemSelected: Int = 0
  var archetype: Archetype = _
  var initialized = false
  var healing = 0f
  var healingFactor = 0.1f
  var lastStrike: String = "Unknown Forces"
  var equipment: Equipment = new Equipment
  var resting = false
  var name = ""
  var dead = false
  var stats: Stats = basePlayerStats()
  var inCombat = false
  var game: Game = _
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  var speed = .25f
  var clickInInv = false
  var clickTick = 0f
  def initially(gme: Game): Unit = {
    game = gme
    val weapon = makeCommonWeapon(0, game, 1, 6)
    weapon.possessor = Some(this)
    weapon.game = game
    game.items = weapon :: game.items
    equipment.weapon = Some(weapon)
    val potion: HealingPotion = HealingPotion()
    potion.number = 10
    potion.possessor = Some(this)
    potion.game = game
    game.items = potion :: game.items
    archetype.onSelect(game)
    stats.health = stats.maxHealth
    initialized = true
  }
  def playerIcon: TextureWrapper =
    TextureWrapper.load(s"Player${archetype.metaArchName}.png")
  def levelUp(): Unit = {
    game.addMessage("You levelled up")
    stats.exp -= stats.nextExp
    stats.nextExp *= 2
    stats.maxHealth += d(2, 5)
    archetype.onLevelUp(game)
    stats.health = stats.maxHealth
    stats.level += 1
  }
  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }
  def attack(target: Enemy): Unit = {
    if (equipment.weapon.nonEmpty) {
      equipment.weapon.foreach(w => w.onAttack(this, target))
    } else {
      if (d(10) > target.stats.ac) {
        target.stats.health -= 1
      }
    }
  }
  def draw(batch: PolygonSpriteBatch) = {
    batch.setColor(Color.WHITE)
    batch.draw(
      playerIcon,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  def update(delta: Float) = {
    if (!yourTurn) {
      tick += delta
      if (tick > speed) {
        yourTurn = true
        tick = 0f
      }
    }
    clickTick += delta
    if (clickTick > .325f) {
      clickTick = 0f
      clickInInv = false
    }
    if (stats.health <= 0) dead = true
    if (!game.inInventory && !game.inCharacterSheet) gameControl(delta)
    else if (game.inInventory && !clickInInv)
      clickInInv = inventoryControl(delta)
    else if (game.inCharacterSheet) charSheetControl(delta)
  }
  def gameControl(delta: Float) = {
    if (resting) speed = .005f else speed = .25f
    if (healing > 4 && stats.health < stats.maxHealth) {
      stats.health += 1
      healing = 0
    }
    if (stats.exp >= stats.nextExp) {
      levelUp()
    }
    if (
      game.enemies.exists(e => {
        val path = Pathfinding.findPath(e.location, location, game.level)
        var dist = Int.MaxValue
        path.foreach(p => {
          dist = p.list.length
        })
        dist < stats.sightRad
      })
    ) inCombat = true
    else inCombat = false
    if (yourTurn) {
      game.keysDown
        .find(key => Character.isDigit(Keys.toString(key).charAt(0)))
        .foreach(n => {
          var keyn = Keys.toString(n).toInt
          if (stats.skills.length >= keyn) stats.skills(keyn - 1) match {
            case range: rangedSkill => {
              if (range.ccd == 0) {
                range
                  .selectTarget(game, this)
                  .foreach(t => range.onUse(this, t, game))
                range.ccd = range.coolDown
                if (range.takesTurn) {
                  yourTurn = false
                  game.enemyTurn = true
                }
              }
            }
          }
        })

      if (game.keysDown.contains(Keys.S) || game.keysDown.contains(Keys.DOWN)) {
        destination.y = location.y - 1
        destination.x = location.x
      } else if (
        game.keysDown.contains(Keys.W) || game.keysDown.contains(Keys.UP)
      ) {
        destination.y = location.y + 1
        destination.x = location.x
      } else if (
        game.keysDown.contains(Keys.D) || game.keysDown.contains(Keys.RIGHT)
      ) {
        destination.y = location.y
        destination.x = location.x + 1
      } else if (
        game.keysDown.contains(Keys.A) || game.keysDown.contains(Keys.LEFT)
      ) {
        destination.y = location.y
        destination.x = location.x - 1
      } else if (game.keysDown.contains(Keys.SPACE)) {
        resting = true
        game.enemyTurn = true
      } else if (game.clicked) {
        destination = game.mouseLocOnGrid.copy()
      } else if (game.keysDown.contains(Keys.R)) {
        resting = true
      } else if (
        game.keysDown.contains(Keys.PERIOD) && (game.keysDown.contains(
          Keys.SHIFT_RIGHT
        ) || game.keysDown.contains(Keys.SHIFT_LEFT))
      ) {
        tryToGoDown()
      } else if (game.keysDown.contains(Keys.G)) {
        game.items.foreach(ite => {
          ite.location.foreach(l => {
            if (l == location) {
              ite.pickUp(this)
              game.addMessage(s"You picked up x${ite.number} ${ite.name}")
            }
          })
        })
      }
    }
    if ((destination != location || resting) && yourTurn) {
      if (!resting) {
        val path = Pathfinding.findPath(location, destination, game.level)
        path.foreach(p => {
          val dest = p.list.reverse(1).copy()
          val enemy = game.enemies.filter(e => e.location == dest)
          if (enemy.isEmpty) {
            location = dest.copy()
          } else {
            attack(enemy.head)
            destination = location.copy()
          }
        })
      } else {
        destination = location.copy()
        if (!inCombat && stats.health < stats.maxHealth)
          (healing += healingFactor * 10)
      }
      if (stats.health < stats.maxHealth) healing += healingFactor
      yourTurn = false
      game.enemyTurn = true
    }
    if (resting && (stats.health == stats.maxHealth || inCombat)) {
      resting = false
    }
  }
  def inventoryControl(delta: Float): Boolean = {
    var clicked = false
    var inventory = game.items
      .filter(i => i.possessor.contains(this))
      .filter(n => n.tNum >= 1)
    if (inventory.nonEmpty) {
      if (inventory.length < inventoryItemSelected) {
        inventoryItemSelected -= 1
      }

      if (game.keysDown.contains(Keys.DOWN) || game.keysDown.contains(Keys.S)) {
        inventoryItemSelected = (inventoryItemSelected + 1) % inventory.length
        clicked = true
      }
      if (game.keysDown.contains(Keys.UP) || game.keysDown.contains(Keys.W)) {
        inventoryItemSelected =
          (inventoryItemSelected + inventory.length - 1) % inventory.length
        clicked = true
      }
      if (yourTurn) {
        if (
          game.keysDown
            .contains(Keys.ENTER) || game.keysDown.contains(Keys.SPACE)
        ) {
          if (inventory.length >= inventoryItemSelected) {
            inventory(inventoryItemSelected).use(this)
          }
          clicked = true
          yourTurn = false
          game.enemyTurn = true
        }
      }
    }
    clicked
  }
  def charSheetControl(delta: Float) = {}
}
