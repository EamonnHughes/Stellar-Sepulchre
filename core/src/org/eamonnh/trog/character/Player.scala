package org.eamonnh.trog.character

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.Square
import org.eamonnh.trog.inGameUserInterface.{inCharacterSheet, inInventory}
import org.eamonnh.trog.items.MedKit
import org.eamonnh.trog.procgen.{ClosedDoor, Floor}
import org.eamonnh.trog.scenes.Game
import org.eamonnh.trog.util.Animation
import org.eamonnh.trog.{Actor, Pathfinding, Trog, Vec2, d, getIfromVec2, getVec2fromI, screenUnit}

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
  var exploring = false
  var stats: Stats = basePlayerStats()
  var inCombat = false
  var rangedSkillUsing: Option[rangedSkill] = None
  var rangedSkillTargetables: List[Vec2] = List.empty
  var selectedSkillLoc: Option[Vec2] = None
  var game: Game = _
  var location: Vec2 = Vec2(0, 0)
  var destination: Vec2 = Vec2(0, 0)
  var yourTurn = true
  var tick = 0f
  var speed = .25f
  var clickInInv = false
  var clickTick = 0f
  var getVisible: List[Vec2] = List.empty
  var movedSkillTarget = false
  var skillActivated = false

  def clearRangedStuff(): Unit = {
    rangedSkillUsing = None
    rangedSkillTargetables = List.empty
    movedSkillTarget = false
    selectedSkillLoc = None
    skillActivated = false
  }

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
    selectedSkillLoc.foreach(loc => {
      if (
        !rangedSkillTargetables.contains(loc)
      ) {
        batch.setColor(1f, 0f, 0, .75f)
      } else if(
        (rangedSkillUsing.head.mustTargetEnemy && !game.enemies.exists(e => e.location == loc)) ||
          (!rangedSkillUsing.head.canTargetEnemy && game.enemies.exists(e => e.location == loc))
      ){
      batch.setColor(1f, 1f, 0, .75f)
      }  else {
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

    if (!yourTurn) {
      tick += delta
      if (tick >= speed || resting || exploring) {
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
    if (!inInventory && !inCharacterSheet) gameControl(delta)
    else if (inInventory && !clickInInv)
      clickInInv = inventoryControl(delta)
    else if (inCharacterSheet) charSheetControl(delta)
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
        val path = Pathfinding.findPath(e.location, location, game.level)
        var dist = Int.MaxValue
        path.foreach(p => {
          dist = p.list.length
        })
        dist < stats.sightRad
      })
    ) {
      inCombat = true
    } else inCombat = false
    if (yourTurn) {
      if (statuses.stunned > 0) {
        statuses.stunned -= 1
        yourTurn = false
        game.enemyTurn = true
      } else {
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
          if (
            game.level.terrains.zipWithIndex.exists({ case (w, i) =>
              !game.explored.contains(getVec2fromI(i, game.level)) && w._1
                .isInstanceOf[Floor]
            })
          ) {
            if(game.level.terrains.zipWithIndex.exists { case (w, i) =>
              !game.explored.contains(getVec2fromI(i, game.level)) && w._1
                .isInstanceOf[Floor] && Pathfinding
                .findPath(location, getVec2fromI(i, game.level), game.level).nonEmpty
            }){
            var dest =
              game.level.terrains.zipWithIndex
                .filter({ case (w, i) =>
                  !game.explored.contains(getVec2fromI(i, game.level)) && w._1
                    .isInstanceOf[Floor] && Pathfinding
                    .findPath(location, getVec2fromI(i, game.level), game.level).nonEmpty
                })
                .minBy({ case (w, i) =>
                  Pathfinding
                    .findPath(location, getVec2fromI(i, game.level), game.level)
                    .head
                    .list
                    .length
                })
            destination = getVec2fromI(dest._2, game.level)
          } else if(game.level.terrains.zipWithIndex.exists { case (w, i) =>
              w._1
                .isInstanceOf[ClosedDoor] && getVec2fromI(i, game.level).getAdjacents.exists(a => Pathfinding
                .findPath(location, a, game.level).nonEmpty)
            }) {
              var dest: Vec2 = game.level.terrains.zipWithIndex
                  .filter({ case (w, i) =>
                    w._1
                      .isInstanceOf[ClosedDoor] && getVec2fromI(i, game.level).getAdjacents.exists(a => Pathfinding
                      .findPath(location, a, game.level).nonEmpty)
                  }).map(i => getVec2fromI(i._2, game.level)).head.getAdjacents.filter(adj => Pathfinding.findPath(location, adj, game.level).nonEmpty).head
              destination = dest
            } else exploring = false
          } else if (location != game.level.downLadder) {
            destination = game.level.downLadder.copy()
            game.addMessage("Floor explored, heading to exit")
          }
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
          exploring = false
          game.enemyTurn = true
        } else if (game.clicked) {
          destination = game.mouseLocOnGrid.copy()
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
          if(destination.getAdjacents.contains(location)) game.level.terrains(getIfromVec2(destination, game.level))._1.onWalkOnTo(destination, game.level)
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
        if (stats.health < stats.maxHealth) healing += healingFactor
        yourTurn = false
        if (initLoc != location) {
          getVisible = game.level.terrains.zipWithIndex
            .filter({ case (t, i) =>
              t._1.walkable &&
              Pathfinding
                .findPath(location, getVec2fromI(i, game.level), game.level).exists(p => p.list.length < stats.sightRad)
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
        game.enemyTurn = true
      }
      if (resting && (stats.health == stats.maxHealth || inCombat)) {
        resting = false
      }
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

  def doRangedSkill(skill: rangedSkill): Unit = {
    var areaOfUse = game.level.terrains.zipWithIndex.filter(t => Pathfinding.findPath(location, getVec2fromI(t._2, game.level), game.level).nonEmpty).map(i => getVec2fromI(i._2, game.level)).toList
    rangedSkillTargetables = game.level.terrains.zipWithIndex.filter(t => Pathfinding.findPath(location,  getVec2fromI(t._2, game.level), game.level).exists(p => p.list.length <= skill.maxRange && p.list.length >= skill.minRange)).map(i => getVec2fromI(i._2, game.level)).toList
    if (rangedSkillTargetables.isEmpty) {
      clearRangedStuff()
    } else {
      if(!skillActivated){
      if(rangedSkillTargetables.exists(t => game.enemies.exists(e => e.location == t) && skill.canTargetEnemy)) {
        selectedSkillLoc = Some(rangedSkillTargetables.filter(t => game.enemies.exists(e => e.location == t)).head)
      } else {
        selectedSkillLoc = Some(rangedSkillTargetables.minBy(t => Pathfinding.findPath(location, t, game.level).head.list.length))
      }
      skillActivated = true
      }
      if (game.keysDown.contains(Keys.LEFT)) {
        if (!movedSkillTarget) {
          var newTarget = Vec2(selectedSkillLoc.head.x - 1, selectedSkillLoc.head.y)
          if(areaOfUse.contains(newTarget)) selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.RIGHT)) {
        if (!movedSkillTarget) {
          var newTarget = Vec2(selectedSkillLoc.head.x + 1, selectedSkillLoc.head.y)
          if(areaOfUse.contains(newTarget)) selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.UP)) {
        if (!movedSkillTarget) {
          var newTarget = Vec2(selectedSkillLoc.head.x, selectedSkillLoc.head.y+1)
          if(areaOfUse.contains(newTarget)) selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else if (game.keysDown.contains(Keys.DOWN)) {
        if (!movedSkillTarget) {
          var newTarget = Vec2(selectedSkillLoc.head.x, selectedSkillLoc.head.y-1)
          if(areaOfUse.contains(newTarget)) selectedSkillLoc = Some(newTarget.copy())
        }
        movedSkillTarget = true
      } else {
        movedSkillTarget = false
      }
      if (game.keysDown.contains(Keys.ENTER) &&
        (!skill.mustTargetEnemy || selectedSkillLoc.forall(l => game.enemies.exists(e => e.location == l))) &&
        (skill.canTargetEnemy || selectedSkillLoc.forall(l => !game.enemies.exists(e => e.location == l))) && rangedSkillTargetables.contains(selectedSkillLoc.head)
      ) {
        skill.onUse(
          this,
          selectedSkillLoc.head,
          game
        )
        skill.ccd = skill.coolDown
        if (skill.takesTurn) {
          yourTurn = false
          game.enemyTurn = true
        }
        clearRangedStuff()
      }
      if (game.keysDown.contains(Keys.ESCAPE)) {
        clearRangedStuff()
      }
    }
  }

  def charSheetControl(delta: Float) = {}
}
