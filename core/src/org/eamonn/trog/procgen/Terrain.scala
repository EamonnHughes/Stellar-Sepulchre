package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.{PolygonSprite, PolygonSpriteBatch}
import org.eamonn.trog.Trog.{garbage, mkTileImage}
import org.eamonn.trog.{Trog, Vec2, screenUnit}
import org.eamonn.trog.util.TextureWrapper

import java.awt.Polygon

trait Terrain {
def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit
val walkable: Boolean
}

case class Floor() extends Terrain {
  override def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit = {
    batch.draw(mkTileImage("ft", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
  val walkable = true
}

case class Wall() extends Terrain {

  override def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit = {
    batch.draw(mkTileImage("wt", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
  val walkable = false
}

case class LadderUp() extends Terrain {
  override def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit = {
    batch.draw(mkTileImage("ft", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
    batch.draw(mkTileImage("ue", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
  val walkable = true
}

case class LadderDown() extends Terrain {
  override def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit = {
    batch.draw(mkTileImage("ft", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
    batch.draw(mkTileImage("de", theme, number), location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
  val walkable = true
}

case class Emptiness() extends Terrain {
  override def draw(batch: PolygonSpriteBatch, location: Vec2, theme: Theme, number: Number): Unit = {
    batch.setColor(Color.BLACK)
    batch.draw(Trog.Square, location.x * screenUnit, location.y * screenUnit, screenUnit, screenUnit)
  }
  val walkable = false
}