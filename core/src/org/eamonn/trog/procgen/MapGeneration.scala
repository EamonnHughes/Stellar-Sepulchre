package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.{Trog, Vec2, screenUnit}

import scala.util.Random

object MapGeneration {
  def getRandomPointInCircle(radius: Int): Vec2 = {
    val t = 2 * math.Pi * math.random()
    val u = math.random() + math.random()
    var r: Double = 0
    if (u > 1) r = 2 - u else r = u
    Vec2(
      (radius * r * math.cos(t)).floor.toInt,
      (radius * r * math.sin(t)).floor.toInt
    )
  }
}

case class GeneratedMap(dimensions: Int) {

  var rooms: List[Room] = List.empty

  def draw(batch: PolygonSpriteBatch): Unit = {
    rooms.foreach(r => {
      batch.setColor(Color.BLUE)
      batch.draw(Trog.Square, r.location.x*screenUnit, r.location.y*screenUnit, r.size.x*screenUnit, r.size.y*screenUnit)
      batch.setColor(Color.WHITE)
      batch.draw(Trog.Square, r.location.x*screenUnit + 1f, r.location.y*screenUnit + 1f, r.size.x*screenUnit - 2f, r.size.y*screenUnit - 2f)
    })
  }
  def generate(): Unit = {
    for (i <- 0 until (dimensions*5/4).toInt) {
      var room = Room(
        MapGeneration.getRandomPointInCircle((dimensions * .4f).toInt),
        Vec2(Random.nextInt(6) + 2, Random.nextInt(6) + 2)
      )
      room.location.x += (dimensions/2) - (room.size.x / 2).toInt
      room.location.y += (dimensions/2) - (room.size.y / 2).toInt
      rooms = room :: rooms
    }/*
    while(rooms.exists(r => rooms.exists(r2 => r.doesOverlap(r2)))){
    rooms.foreach(r => {
      var dX = 0
      var dY = 0
      rooms.filter(ro => ro.doesOverlap(r)).foreach(ro => {
        dX += (r.location.x - ro.location.x)
        dY += (r.location.y - ro.location.y)
      })
      r.location.x += dX.sign
      r.location.y += dY.sign
    })
    }*/
  }

}

case class Room(location: Vec2, size: Vec2) {
  def doesOverlap(room2: Room): Boolean = {
    var onX = (location.x + size.x > room2.location.x && location.x < room2.location.x + room2.size.x)
    var onY = (location.y + size.y > room2.location.y && location.y < room2.location.y + room2.size.y)
    (onX && onY)
  }
}
