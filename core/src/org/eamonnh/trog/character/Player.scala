package org.eamonnh.trog.character

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.{Highlight, Square}
import org.eamonnh.trog.inGameUserInterface.{inCharacterSheet, inInventory}
import org.eamonnh.trog.items.MedKit
import org.eamonnh.trog.procgen.{ClosedDoor, Floor}
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.Animation
import org.eamonnh.trog.{Actor, Pathfinding, Trog, Vec2, d, getIfromVec2, getVec2fromI, screenUnit}

import scala.util.Random

case class Player() extends Actor {
  var menuItemSelected: Int = 0
  var archetype: Archetype = _
  var initialized = false
  var healing = 0f
  var healingFactor = 0.1f
  var lastStrike: String = "Unknown Forces"
  var equipment: Equipment = new Equipment
  var resting = false
  var name = ""
  var dead = false
  var exploring = false
  var stats: Stats = basePlayerStats()
  var inCombat = false
  var rangedSkillUsing: Option[rangedSkill] = None
  var rangedSkillTargetables: List[Vec2] = List.empty
  var selectedSkillLoc: Option[Vec2] = None
  var game: Game = _
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var turn = true
  var tick = 0f
  var speed = .25f
  var clickInInv = false
  var clickTick = 0f
  var getVisible: List[Vec2] = List.empty
  var movedSkillTarget = false
  var skillActivated = false
  var perks: List[Perk] = List.empty
  var perkPool: List[Perk] = List.empty
  var perkChoices: List[Perk] = List.empty
  var inPerkChoice = false
  var perkClicked = false

  def initially(gme: Game): Unit = {
    game = gme
    val medKit: MedKit = MedKit()
    medKit.number = 10
    medKit.possessor = Some(this)
    medKit.game = game
    game.items = medKit :: game.items
    archetype.onSelect(game)
    stats.health = stats.maxHealth
    initialized = true
  }

  def draw(batch: PolygonSpriteBatch) = {

    if (stats.health > 0) {
      batch.setColor(Color.RED)
      batch.draw(
        Square,
        location.x * screenUnit,
        location.y * screenUnit,
        screenUnit * stats.health / stats.maxHealth,
        screenUnit * .05f
      )
    }
    batch.setColor(Color.WHITE)
    Animation.fourFrameAnimation(
      game,
      batch,
      playerIcon,
      location.x.toFloat,
      location.y.toFloat
    )
    rangedSkillTargetables.foreach(t => {
      batch.setColor(0f, .5f, 0f, .35f)
      batch.draw(
        Highlight,
        t.x * screenUnit,
        t.y * screenUnit,
        screenUnit,
        screenUnit
      )
    })
    selectedSkillLoc.foreach(loc => {
      if (!rangedSkillTargetables.contains(loc)) {
        batch.setColor(1f, 0f, 0, .75f)
      } else if (
        (rangedSkillUsing.head.mustTargetEnemy && !game.enemies
          .exists(e => e.location == loc)) ||
        (!rangedSkillUsing.head.canTargetEnemy && game.enemies
          .exists(e => e.location == loc))
      ) {
        batch.setColor(1f, 1f, 0, .75f)
      } else {
        batch.setColor(0f, .5f, 0, .75f)
      }
      Animation.twoFrameAnimation(
        game,
        batch,
        "targetReticle",
        loc.x.toFloat,
        loc.y.toFloat
      )
    })
  }

  def playerIcon: String = s"Player${archetype.metaArchName}"

  def update(delta: Float): Unit = {

    if (!turn) {
      tick += delta
      if ((tick >= speed || resting || exploring) && !game.playerTurnDone) {
        turn = true
        tick = 0f
      }
    }
    clickTick += delta
    if (clickTick > .5f || game.keysDown.isEmpty) {
      clickTick = 0f
      clickInInv = false
      perkClicked = false
    }
    if (stats.health <= 0) dead = true
    if (!inInventory && !inCharacterSheet && !inPerkChoice) gameControl(delta)
    else if (inInventory && !clickInInv)
      clickInInv = inventoryControl()
    else if (inCharacterSheet) charSheetControl(delta) else if (inPerkChoice && !perkClicked) perkClicked = perkSelectControl()
  }
  def perkSelectControl(): Boolean = {
    var clicked = false
    if(perkChoices.isEmpty)     perkChoices = Random.shuffle(perkPool.filter(i => i.isAllowed(this))).take(5)
    if (perkChoices.nonEmpty) {
      while (perkChoices.length < menuItemSelected) {
        menuItemSelected -= 1
      }

      if (game.keysDown.contains(Keys.DOWN) || game.keysDown.contains(Keys.S)) {
        menuItemSelected = (menuItemSelected + 1) % perkChoices.length
        clicked = true
      }
      if (game.keysDown.contains(Keys.UP) || game.keysDown.contains(Keys.W)) {
        menuItemSelected =
          (menuItemSelected + perkChoices.length - 1) % perkChoices.length
        clicked = true
      }
        if (
          game.keysDown
            .contains(Keys.ENTER) || game.keysDown.contains(Keys.SPACE)
        ) {
            perkChoices(menuItemSelected).onApply(this)
            perks = perkChoices(menuItemSelected):: perks
            perkPool = perkPool.filterNot(_ eq perkChoices(menuItemSelected))
            perkChoices = List.empty
            inPerkChoice = false
             clicked = true
        }
    }
    clicked
  }

  def gameControl(delta: Float) = {
    var initLoc = location.copy()
    if (inCombat) {
      destination = location.copy()
      exploring = false
    }
    if (healing > 4 && stats.health < stats.maxHealth) {
      stats.health += 1
      healing = 0
    }
    if (stats.exp >= stats.nextExp) {
      levelUp()
    }
    if (
      game.enemies.exists(e => {
        val path = Pathfinding.findRaycastPath(e.location, location, game.level)
        var dist = Int.MaxValue
        path.foreach(p => {
          dist = p.list.length
        })
        dist < stats.sightRad
      })
    ) {
      inCombat = true
    } else inCombat = false
    if (turn) {
      statuses.foreach(s => {
        s.onTick(this)
        s.timeLeft -= 1
        if(s.timeLeft == 0) statuses = statuses.filterNot( s2 => s2 eq s)
      })}
    if (turn) {
      game.keysDown
          .find(key => Character.isDigit(Keys.toString(key).charAt(0)))
          .foreach(n => {
            var keyn = Keys.toString(n).toInt
            if (stats.skills.length >= keyn && keyn > 0)
              stats.skills(keyn - 1) match {
                case range: rangedSkill => {
                  if (range.ccd == 0) {
                    rangedSkillUsing = Some(range)
                  }
                }
              }
          })
        if (game.keysDown.contains(Keys.Z) && !inCombat) {
          exploring = !exploring
          if (!exploring) destination = location.copy()
        }
        if (exploring && destination == location) {
          autoExplore()
        }
        if (
          game.level.terrains.zipWithIndex.forall({ case (w, i) =>
            !w._1.isInstanceOf[Floor] || game.explored
              .contains(getVec2fromI(i, game.level))
          })
        )
          exploring = false
        if (
          game.keysDown.contains(Keys.S) || game.keysDown.contains(Keys.DOWN)
        ) {
          destination.y = location.y - 1
          destination.x = location.x
          exploring = false
        } else if (
          game.keysDown.contains(Keys.W) || game.keysDown.contains(Keys.UP)
        ) {
          destination.y = location.y + 1
          destination.x = location.x
          exploring = false
        } else if (
          game.keysDown.contains(Keys.D) || game.keysDown.contains(Keys.RIGHT)
        ) {
          destination.y = location.y
          destination.x = location.x + 1
          exploring = false
        } else if (
          game.keysDown.contains(Keys.A) || game.keysDown.contains(Keys.LEFT)
        ) {
          destination.y = location.y
          destination.x = location.x - 1
          exploring = false
        } else if (game.keysDown.contains(Keys.SPACE)) {
          resting = true
          exploring = false
          turn = false
          game.playerTurnDone = true
        } else if (game.clicked) {
          destination = game.mouseLocOnGrid.copy()
          exploring = false
        } else if (game.keysDown.contains(Keys.R)) {
          resting = true
          exploring = false
        } else if (
          game.keysDown.contains(Keys.PERIOD) && (game.keysDown.contains(
            Keys.SHIFT_RIGHT
          ) || game.keysDown.contains(Keys.SHIFT_LEFT))
        ) {
          tryToGoDown()
        } else if (game.keysDown.contains(Keys.G)) {
          exploring = false
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
      if ((destination != location || resting) && turn) {
        if (!resting) {
          if (destination.getAdjacents.contains(location))
            game.level
              .terrains(getIfromVec2(destination, game.level))
              ._1
              .onWalkOnTo(destination, game.level)
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
            if (!exploring)
              Trog.Crunch.play(.5f, 1 + ((Math.random() / 4) - .125).toFloat, 0)
          })
        } else {
          destination = location.copy()
          if (!inCombat && stats.health < stats.maxHealth)
            (healing += healingFactor * 10)
        }
        if (stats.health < stats.maxHealth){ healing += healingFactor}
        turn = false
        game.playerTurnDone = true
        if (initLoc != location) {
          getVisible = game.level.terrains.zipWithIndex
            .filter({ case (t, i) =>
              t._1.walkable &&
                Pathfinding.findRaycastPathUpTo(location, getVec2fromI(i, game.level), game.level)
                  .exists(p => p.list.length < stats.sightRad)
            })
            .map(t => getVec2fromI(t._2, game.level))
            .toList
        }

        getVisible
          .foreach(w => {
            if (!game.explored.contains(w)) {
              game.explored = w :: game.explored
            }
          })
      }
      if (resting && (stats.health == stats.maxHealth || inCombat)) {
        resting = false
      }
  }

  def levelUp(): Unit = {
    game.addMessage("You levelled up")
    stats.exp -= stats.nextExp
    stats.nextExp *= 2
    stats.maxHealth += d(2, 5)
    archetype.onLevelUp(game)
    stats.health = stats.maxHealth
    stats.level += 1
    Trog.Jingle.play(.4f)
    if (stats.level > 1) {
      game.lvlUping = true
    }
  }

  def tryToGoDown(): Unit = {
    if (location == game.level.downLadder) game.descending = true
  }

  def attack(target: Actor): Unit = {
    if (equipment.weapon.nonEmpty) {
      equipment.weapon.foreach(w => w.onAttack(this, target))
    } else {
      if (d(10) > target.stats.ac) {
        target.stats.health -= 1
      }
    }
  }

  def inventoryControl(): Boolean = {
    var clicked = false
    var inventory = game.items
      .filter(i => i.possessor.contains(this))
      .filter(n => n.tNum >= 1)
    if (inventory.nonEmpty) {
      if (inventory.length < menuItemSelected) {
        menuItemSelected -= 1
      }

      if (game.keysDown.contains(Keys.DOWN) || game.keysDown.contains(Keys.S)) {
        menuItemSelected = (menuItemSelected + 1) % inventory.length
        clicked = true
      }
      if (game.keysDown.contains(Keys.UP) || game.keysDown.contains(Keys.W)) {
        menuItemSelected =
          (menuItemSelected + inventory.length - 1) % inventory.length
        clicked = true
      }
      if (turn) {
        if (
          game.keysDown
            .contains(Keys.ENTER) || game.keysDown.contains(Keys.SPACE)
        ) {
          if (inventory.length >= menuItemSelected) {
            inventory(menuItemSelected).use(this)
          }
          clicked = true
          turn = false
          game.playerTurnDone = true
        }
      }
    }
    clicked
  }

  def charSheetControl(delta: Float) = {}

  def doRangedSkill(skill: rangedSkill): Unit = {
    var areaOfUse = game.level.terrains.zipWithIndex
      .filter(t =>
        Pathfinding
          .findPath(location, getVec2fromI(t._2, game.level), game.level)
          .nonEmpty
      )
      .map(i => getVec2fromI(i._2, game.level))
      .toList
    rangedSkillTargetables = game.level.terrains.zipWithIndex
      .filter(t =>
        Pathfinding
          .findRaycastPath(location, getVec2fromI(t._2, game.level), game.level)
          .exists(p =>
            p.list.length <= skill.maxRange(this) && p.list.length >= skill.minRange
          )
      )
      .map(i => getVec2fromI(i._2, game.level))
      .toList
    if (rangedSkillTargetables.isEmpty) {
      clearRangedStuff()
    } else {
      if (!skillActivated) {
        if (
          rangedSkillTargetables.exists(t =>
            game.enemies.exists(e => e.location == t) && skill.canTargetEnemy
          )
        ) {
          selectedSkillLoc = Some(
            rangedSkillTargetables
              .filter(t => game.enemies.exists(e => e.location == t))
              .head
          )
        } else {
          selectedSkillLoc = Some(
            rangedSkillTargetables.minBy(t =>
              Pathfinding.findRaycastPath(location, t, game.level).head.list.length
            )
          )
        }
        skillActivated = true
      }
      if (game.keysDown.contains(Keys.LEFT)) {
        if (!movedSkillTarget) {
          var newTarget =
            Vec2(selectedSkillLoc.head.x - 1, selectedSkillLoc.head.y)
          if (areaOfUse.contains(newTarget))
            selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.RIGHT)) {
        if (!movedSkillTarget) {
          var newTarget =
            Vec2(selectedSkillLoc.head.x + 1, selectedSkillLoc.head.y)
          if (areaOfUse.contains(newTarget))
            selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.UP)) {
        if (!movedSkillTarget) {
          var newTarget =
            Vec2(selectedSkillLoc.head.x, selectedSkillLoc.head.y + 1)
          if (areaOfUse.contains(newTarget))
            selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.DOWN)) {
        if (!movedSkillTarget) {
          var newTarget =
            Vec2(selectedSkillLoc.head.x, selectedSkillLoc.head.y - 1)
          if (areaOfUse.contains(newTarget))
            selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else {
        movedSkillTarget = false
      }
      if (
        game.keysDown.contains(Keys.ENTER) &&
        (!skill.mustTargetEnemy || selectedSkillLoc
          .forall(l => game.enemies.exists(e => e.location == l))) &&
        (skill.canTargetEnemy || selectedSkillLoc.forall(l =>
          !game.enemies.exists(e => e.location == l)
        )) && rangedSkillTargetables.contains(selectedSkillLoc.head)
      ) {
        skill.onUse(
          this,
          selectedSkillLoc.head,
          game
        )
        skill.ccd = skill.coolDown
        if (skill.takesTurn) {
          turn = false
          game.playerTurnDone = true
        }
        clearRangedStuff()
      }
      if (game.keysDown.contains(Keys.ESCAPE)) {
        clearRangedStuff()
      }
    }
  }

  def clearRangedStuff(): Unit = {
    rangedSkillUsing = None
    rangedSkillTargetables = List.empty
    movedSkillTarget = false
    selectedSkillLoc = None
    skillActivated = false
  }

  def autoExplore(): Unit = {
    if (
      game.level.terrains.zipWithIndex.exists({ case (w, i) =>
        !game.explored.contains(getVec2fromI(i, game.level)) && w._1
          .isInstanceOf[Floor]
      })
    ) {
      if (
        game.level.terrains.zipWithIndex.exists { case (w, i) =>
          !game.explored.contains(getVec2fromI(i, game.level)) && w._1
            .isInstanceOf[Floor] && Pathfinding
            .findPath(location, getVec2fromI(i, game.level), game.level)
            .nonEmpty
        }
      ) {
        var dest =
          game.level.terrains.zipWithIndex
            .filter({ case (w, i) =>
              !game.explored.contains(getVec2fromI(i, game.level)) && w._1
                .isInstanceOf[Floor] && Pathfinding
                .findPath(
                  location,
                  getVec2fromI(i, game.level),
                  game.level
                )
                .nonEmpty
            })
            .minBy({ case (w, i) =>
              Pathfinding
                .findPath(
                  location,
                  getVec2fromI(i, game.level),
                  game.level
                )
                .head
                .list
                .length
            })
        destination = getVec2fromI(dest._2, game.level)
      } else if (
        game.level.terrains.zipWithIndex.exists { case (w, i) =>
          w._1
            .isInstanceOf[ClosedDoor] && getVec2fromI(
            i,
            game.level
          ).getAdjacents.exists(a =>
            Pathfinding
              .findPath(location, a, game.level)
              .nonEmpty
          )
        }
      ) {
        var dest: Vec2 = game.level.terrains.zipWithIndex
          .filter({ case (w, i) =>
            w._1
              .isInstanceOf[ClosedDoor] && getVec2fromI(
              i,
              game.level
            ).getAdjacents.exists(a =>
              Pathfinding
                .findPath(location, a, game.level)
                .nonEmpty
            )
          })
          .map(i => getVec2fromI(i._2, game.level))
          .head
          .getAdjacents
          .filter(adj =>
            Pathfinding.findPath(location, adj, game.level).nonEmpty
          )
          .head
        destination = dest
      } else exploring = false
    } else if (location != game.level.downLadder) {
      destination = game.level.downLadder.copy()
      game.addMessage("Floor explored, heading to exit")
    }
  }
}
