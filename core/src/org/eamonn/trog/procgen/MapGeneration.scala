package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Pathfinding, Trog, Vec2, screenUnit}

import scala.util.Random

object MapGeneration {
  def fullyWalkableLevel(dim: Int): Level = {
    var l = new Level
    l.dimensions = dim
    for (x <- 0 until dim) {
      for (y <- 0 until dim) {
        l.walkables = Vec2(x, y) :: l.walkables
      }
    }
    l
  }
}

case class GeneratedMap(
                         dimensions: Int,
                         roomMin: Int,
                         roomMax: Int,
                         roomDensity: Float
                       ) {

  var rooms: List[Room] = List.empty
  var mainRooms: List[Room] = List.empty

  def generate(): Boolean = {
    var done = true
    //makes rooms
    if (rooms.isEmpty) {
      done = false
      for (i <- 0 until (dimensions * roomDensity).toInt) {
        val scale = roomMin + Random.nextInt((roomMax - roomMin) / 2)
        var size: Vec2 = Vec2(-100, -100)
        var location: Vec2 = Vec2(-100, -100)
        var tick = 0
        while (
          (size == Vec2(-100, -100) || Room(location, size).getAllTiles.exists(
            t => rooms.exists(r => r.getAllTiles.contains(t))
          )) && tick <= 1000
        ) {
          size = Vec2(
            scale + Random.nextInt((roomMax - roomMin) / 2),
            scale + Random.nextInt((roomMax - roomMin) / 2)
          )
          location = Vec2(
            Random.nextInt(dimensions - size.x),
            Random.nextInt(dimensions - size.y)
          )
          tick += 1
        }
        var room = Room(location, size)
        rooms = room :: rooms
        mainRooms = room :: mainRooms
      }
    }

    setAdjacentsAsConnected()
    var c = true
    while (c) {
      c = connectConnections()
    }
    //println(s"Main Rooms: ${mainRooms.mkString("; ")}")
    val rooml = mainRooms.filter(r =>
      mainRooms
        .filter(r2 => r2 != r)
        .exists(r2 => {
          !r2.connected.contains(r)
        })
    )
    if (rooml.nonEmpty) {
      done = false
      val fr = rooml.minBy(r => r.connected.length)
      //println(s"FR: $fr")
      val sr = mainRooms
        .filter(r2 => (!r2.connected.contains(fr) && r2 != fr))
        .minBy(r2 => {
          Math.sqrt(
            ((fr.location.x - r2.location.x) * (fr.location.x - r2.location.x)) +
              ((fr.location.y - r2.location.y) * (fr.location.y - r2.location.y))
          )
        })
      //println(s"SR: $sr")
      val p = Pathfinding.findHalfPath(
        fr.getAllOnBorder(Random.nextInt(fr.getAllOnBorder.length)),
        sr.getAllOnBorder(Random.nextInt(sr.getAllOnBorder.length)),
        MapGeneration.fullyWalkableLevel(dimensions)
      )
      p.foreach(l => {
        l.list.foreach(loc => rooms = Room(loc, Vec2(1, 1)) :: rooms)
      })
    }
    done
  }

  def setAdjacentsAsConnected(): Unit = {
    rooms.foreach(r => {
      rooms
        .filterNot(r2 => {
          (r2 eq r) || r2.connected.contains(r)
        })
        .foreach(r2 => {
          if (
            r.getAllTiles.exists(t =>
              t.getAdjacents.exists(a => {
                (r2.getAllTiles.exists(t2 =>
                  t2.getAdjacents.exists(a2 => {
                    t == t2 || t == a2 || t2 == a
                  })
                )
                  )
              })
            )
          ) {
            r.connected = r2 :: r.connected
            r2.connected = r :: r2.connected
          }

        })
    })
  }

  def connectConnections(): Boolean = {
    var change = false
    rooms.foreach(r => {
      r.connected.foreach(r2 => {
        r2.connected
          .filterNot(r3 =>
            r.connected.contains(r3) || r3.connected.contains(r) || r3 == r
          )
          .foreach(r3 => {
            r.connected = r3 :: r.connected
            r3.connected = r :: r3.connected
            change = true
          })
      })
    })
    change
  }

  def draw(batch: PolygonSpriteBatch): Unit = {
    rooms.foreach(r => {
      batch.setColor(Color.BLUE)
      batch.draw(
        Trog.Square,
        r.location.x * screenUnit,
        r.location.y * screenUnit,
        r.size.x * screenUnit,
        r.size.y * screenUnit
      )
      batch.setColor(Color.WHITE)
      batch.draw(
        Trog.Square,
        r.location.x * screenUnit + 1f,
        r.location.y * screenUnit + 1f,
        r.size.x * screenUnit - 2f,
        r.size.y * screenUnit - 2f
      )
    })
  }

  def doExport(): Level = {
    var level = new Level
    rooms.foreach(r => {
      r.getAllTiles.foreach(t => level.walkables = t :: level.walkables)
    })
    level.upLadder =
      level.walkables(Random.nextInt(level.walkables.length)).copy()
    level.downLadder = level.walkables
      .filterNot(w => w == level.upLadder)(
        Random.nextInt(
          level.walkables.filterNot(w => w == level.upLadder).length
        )
      )
      .copy()

    level.dimensions = dimensions
    level
  }

}

case class Room(location: Vec2, size: Vec2) {
  val id = Room.nextGid()
  var connected = List.empty[Room]

  def getAllOnBorder: List[Vec2] = {
    var tiles = List.empty[Vec2]
    getAllTiles.foreach(t => {
      if (t.getHalfAdjacents.exists(a => !getAllTiles.contains(a)))
        tiles = t :: tiles
    })
    tiles
  }

  def getAllTiles: List[Vec2] = {
    var tiles = List.empty[Vec2]
    for (x <- location.x until location.x + size.x) {
      for (y <- location.y until location.y + size.y) {
        tiles = Vec2(x, y) :: tiles
      }
    }
    tiles
  }

  override def toString: String = {
    s"Room[$id, connected=[${connected.map(_.id).sorted.mkString(",")}]]"
  }
}

object Room {
  private var gid = 0

  private def nextGid(): Int = {
    val id = gid
    gid = gid + 1
    id
  }
}

case class Connection(rooms: (Room, Room)) {
  def getConnected(connexs: List[Connection]): List[Connection] = {
    connexs
      .filter(c => {
        (c.rooms._1 == rooms._1) ||
          (c.rooms._1 == rooms._2) ||
          (c.rooms._2 == rooms._2) ||
          (c.rooms._2 == rooms._1)
      })
      .filterNot(c => c eq this)
  }

}

class Level extends Serializable {
  var downLadder: Vec2 = _
  var upLadder: Vec2 = _
  var dimensions = 0
  var walkables: List[Vec2] = List.empty

  def draw(batch: PolygonSpriteBatch): Unit = {
    var wallLocs: List[Vec2] = List.empty
    walkables.foreach(w => {
      batch.setColor(Color.WHITE)
      w.getAdjacents.foreach(a => {
        if (!walkables.contains(a)) {
          wallLocs = a :: wallLocs
        }
      })
      batch.draw(
        floorTile,
        w.x * screenUnit,
        w.y * screenUnit,
        screenUnit,
        screenUnit
      )
    })
    wallLocs
      .sortBy(w => -w.y)
      .foreach(a => {
        batch.setColor(1, 1, 1, 1)
        batch.draw(
          wall,
          a.x * screenUnit,
          a.y * screenUnit,
          screenUnit,
          screenUnit
        )
      })
    batch.draw(
      ladderUpTile,
      upLadder.x * screenUnit,
      upLadder.y * screenUnit,
      screenUnit,
      screenUnit
    )
    batch.draw(
      ladderDownTile,
      downLadder.x * screenUnit,
      downLadder.y * screenUnit,
      screenUnit,
      screenUnit
    )

  }

  def floorTile: TextureWrapper = Trog.floorTile

  def ladderUpTile: TextureWrapper = Trog.ladderUpTile

  def ladderDownTile: TextureWrapper = Trog.ladderDownTile

  def wall: TextureWrapper = Trog.Wall
}
