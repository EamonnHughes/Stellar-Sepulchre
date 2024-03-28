package org.eamonnh.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.mkTileImage
import org.eamonnh.trog.{Trog, Vec2, getIfromVec2, screenUnit}

trait Terrain {
  def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit
  val walkable: Boolean
  def onWalkOnTo(location: Vec2, level: Level): Unit = {}
}

case class Floor() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("ft", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = true
}

case class Wall() extends Terrain {

  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("wt", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = false
}

case class LadderUp() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("ft", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
    batch.draw(
      mkTileImage("ue", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = true
}

case class LadderDown() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("ft", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
    batch.draw(
      mkTileImage("de", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = true
}

case class Emptiness() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.setColor(Color.BLACK)
    batch.draw(
      Trog.Square,
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = false
}

case class ClosedDoor() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("ft", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
    batch.draw(
      mkTileImage("dc", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = false

  override def onWalkOnTo(location: Vec2, level: Level): Unit = {
    level.terrains.update(getIfromVec2(location, level), (OpenDoor(), level.terrains(getIfromVec2(location, level))._2))
  }
}

case class OpenDoor() extends Terrain {
  override def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit = {
    batch.draw(
      mkTileImage("ft", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
    batch.draw(
      mkTileImage("do", theme, number),
      location.x * screenUnit,
      location.y * screenUnit,
      screenUnit,
      screenUnit
    )
  }
  val walkable = true
}
