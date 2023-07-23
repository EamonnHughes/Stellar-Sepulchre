package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import jdk.javadoc.internal.doclets.formats.html.markup.Navigation
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.procgen.MapGeneration.fullyWalkableLevel
import org.eamonn.trog.util.TextureWrapper
import org.eamonn.trog.{Path, Pathfinding, Trog, Vec2, connectPath, screenUnit}

import scala.collection.mutable
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

case class GeneratedMap(dimensions: Int, roomMin: Int, roomMax: Int, roomDensity: Float) {

  var rooms: List[Room] = List.empty
  var mainRooms: List[Room] = List.empty
  var connections: List[Connection] = List.empty

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
  var connectTick = 0f
  def connectedTo(r: Room, r2: Room): Boolean = {
    val alreadyUsed = mutable.Set.empty[Connection]
    var tick = 0
    var conLists: List[connectPath] = List.empty
    connections
      .filter(c => c.rooms._1 == r || c.rooms._2 == r)
      .foreach(c => conLists = connectPath(List(c)) :: conLists)
    while (
      !conLists.exists(c =>
        c.list.exists(l => l.rooms._1 == r2 || l.rooms._2 == r2)
      ) && tick < 1000
    ) {
      tick += 1
      conLists.foreach(conOne =>
        for {
          con <- conOne.list
          if (alreadyUsed).add(con)
        } yield conOne.add(con)
      )
    }
    (tick < 1000)
  }
  def sAdjC(): Unit = {
    //sets adjacent rooms as connected
    rooms.foreach(r => {
      rooms
        .filterNot(r2 => r eq r2)
        .foreach(r2 => {
          var level = new Level()
          level.dimensions = dimensions
          rooms.foreach(r => {
            r.getAllTiles.foreach(t => level.walkables = t :: level.walkables)
          })
          if (
              Pathfinding.findHalfPath(r.location, r2.location, level).nonEmpty
          ) connections = Connection((r, r2)) :: connections
        })
    })
  }
  def generate(): Boolean = {
    var done = true

    //creates rooms
    if (rooms.isEmpty) {
      done = false
      for (i <- 0 until (dimensions * roomDensity).toInt) {
        var scale = roomMin + Random.nextInt((roomMax - roomMin)/2)
        var size: Vec2 = Vec2(-100, -100)
        var location: Vec2 = Vec2(-100, -100)
        var tick = 0
        while (
          (size == Vec2(-100, -100) || Room(location, size).getAllTiles.exists(
            t => rooms.exists(r => r.getAllTiles.contains(t))
          )) && tick <= 1000
        ) {
          size = Vec2(scale + Random.nextInt((roomMax - roomMin)/2), scale + Random.nextInt((roomMax - roomMin)/2))
          location = Vec2(
            Random.nextInt(dimensions - size.x),
            Random.nextInt(dimensions - size.y)
          )
          tick += 1
        }
        rooms = Room(location, size) :: rooms
        mainRooms = Room(location, size) :: mainRooms
      }
    }

    sAdjC()

    //generates connections
    if (!isTotallyConnected) {
      done = false
      connectTick += 1
      var r = mainRooms.minBy(r =>
        connections.count(c => c.rooms._1 == r || c.rooms._2 == r)
      )
      var r2 = mainRooms
        .filterNot(r2 => {
          r2 == r || connectedTo(r, r2) || connectedTo(r2, r)
        }).minBy(r2 => {
        Math.sqrt(
          ((r2.location.x - r.location.x) * (r2.location.x - r.location.x)) + ((r2.location.y - r.location.y) * (r2.location.y - r.location.y))
        )
      })
      var shortestPath: Option[Path] = None
      r.getAllOnBorder.foreach(rT =>
        r2.getAllOnBorder.foreach(r2T => {
          var path =
            Pathfinding.findHalfPath(rT, r2T, fullyWalkableLevel(dimensions))
          path.foreach(p => {
            if (shortestPath.nonEmpty) {
              if ((p.list.length < shortestPath.head.list.length))
                shortestPath = Some(p)
            } else {
              shortestPath = Some(p)
            }
          })
        })
      )
      shortestPath.foreach(sp => {
        sp.list.foreach(spL =>
          if (!rooms.exists(r => r.getAllTiles.contains(spL.copy()))) {
            rooms = Room(spL.copy(), Vec2(1, 1)) :: rooms
          }
        )
      })
    }
    done
  }

  def isTotallyConnected: Boolean = mainRooms.forall(r =>
    mainRooms.forall(r2 => {
      connectedTo(r, r2)
    })
  )

  def doExport(): Level = {
    var shiftX = 0 - rooms.minBy(m => m.location.x).location.x
    var shiftY = 0 - rooms.minBy(m => m.location.x).location.x
    rooms.foreach(m => {
      m.location.x += shiftX
      m.location.y += shiftY
    })
    var lengthX = rooms
      .maxBy(m => m.location.x + m.size.x)
      .location
      .x + rooms.maxBy(m => m.location.x + m.size.x).size.x
    var lengthY = rooms
      .maxBy(m => m.location.y + m.size.y)
      .location
      .y + rooms.maxBy(m => m.location.y + m.size.y).size.y
    var level = new Level
    rooms.foreach(r => {
      r.getAllTiles.foreach(t => level.walkables = t :: level.walkables)
    })
    level.dimensions = dimensions
    level
  }

}

case class Room(location: Vec2, size: Vec2) {
  def getAllTiles: List[Vec2] = {
    var tiles = List.empty[Vec2]
    for (x <- location.x until location.x + size.x) {
      for (y <- location.y until location.y + size.y) {
        tiles = Vec2(x, y) :: tiles
      }
    }
    tiles
  }
  def getAllOnBorder: List[Vec2] = {
    var tiles = List.empty[Vec2]
    getAllTiles.foreach(t => {
      if(t.getHalfAdjacents.exists(a => !getAllTiles.contains(a))) tiles = t :: tiles
    })
    tiles
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

class Level {
  var floor: TextureWrapper = TextureWrapper.load("floortile.png")
  var wall: TextureWrapper = TextureWrapper.load("walltile.png")
  var dimensions = 0
  var walkables: List[Vec2] = List.empty
  def draw(batch: PolygonSpriteBatch): Unit = {
    walkables.foreach(w => {
      batch.setColor(Color.WHITE)
      w.getAdjacents.foreach(a => {
        if(!walkables.contains(a)) {
          batch.draw(
            wall,
            a.x * screenUnit,
            a.y * screenUnit,
            screenUnit,
            screenUnit
          )
        }
      })
      batch.draw(
        floor,
        w.x * screenUnit,
        w.y * screenUnit,
        screenUnit,
        screenUnit
      )
    })
  }
}
