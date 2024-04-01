package org.eamonnh.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.Trog.mkTileImage
import org.eamonnh.trog.{Trog, Vec2, getIfromVec2, screenUnit}

trait Terrain {
  val walkable: Boolean

  def draw(
      batch: PolygonSpriteBatch,
      location: Vec2,
      theme: Theme,
      number: Number
  ): Unit

  def onWalkOnTo(location: Vec2, level: Level): Unit = {}
}

case class Floor() extends Terrain {
  val walkable = true

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
}

case class Wall() extends Terrain {

  val walkable = false

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
}

case class LadderUp() extends Terrain {
  val walkable = true

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
}

case class LadderDown() extends Terrain {
  val walkable = true

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
}

case class Emptiness() extends Terrain {
  val walkable = false

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
}

case class ClosedDoor() extends Terrain {
  val walkable = false

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

  override def onWalkOnTo(location: Vec2, level: Level): Unit = {
    level.terrains.update(
      getIfromVec2(location, level),
      (OpenDoor(), level.terrains(getIfromVec2(location, level))._2)
    )
  }
}

case class OpenDoor() extends Terrain {
  val walkable = true

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
}
