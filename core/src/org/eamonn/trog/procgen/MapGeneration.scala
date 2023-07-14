package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch

import scala.util.Random

object MapGeneration {
  def getRandomPointInCircle(radius: Int): (Double, Double) = {
    val t = 2 * math.Pi * math.random()
    val u = math.random() + math.random()
    var r: Double = null
    if (u > 1) r = 2 - u else r = u
    (radius * r * math.cos(t), radius * r * math.sin(t))
  }
}

case class GeneratedMap(dimensions: Int) {

  def draw(batch: PolygonSpriteBatch): Unit = {

  }
  def generate(): Unit = {
  }

}