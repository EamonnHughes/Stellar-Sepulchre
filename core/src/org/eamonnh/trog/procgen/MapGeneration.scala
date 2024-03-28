package org.eamonnh.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonnh.trog.{Pathfinding, Trog, Vec2, getVec2fromI, screenUnit}

import scala.collection.mutable
import scala.util.Random

object MapGeneration {
  def fullyWalkableLevel(dim: Int): Level = {
    var l = new Level
    l.dimensions = dim
    l.terrains = Array.fill(dim * dim)(Floor(), 0)
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
  var locPurpose: mutable.Map[Vec2, PlacePurpose] = mutable.Map.empty

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
            Random.nextInt((dimensions - 1) - size.x) + 1,
            Random.nextInt((dimensions - 1) - size.y) + 1
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

        var place1 = l.list
          .findLast(locut => !rooms.exists(r => r.getAllTiles.contains(locut)))
          .head
        var place2 = l.list
          .find(locut => !rooms.exists(r => r.getAllTiles.contains(locut)))
          .head
        if (
          (!rooms.exists(r => {
            r.getAllTiles.contains(Vec2(place1.x, place1.y + 1))
          }) &&
            !rooms.exists(r =>
              r.getAllTiles.contains(Vec2(place1.x, place1.y - 1))
            ) ) ||
            (!rooms.exists(r => {
            r.getAllTiles.contains(Vec2(place1.x + 1, place1.y))
          })  && !rooms
              .exists(r2 =>
                r2.getAllTiles.contains(Vec2(place1.x - 1, place1.y))
              ))
        ) {
          locPurpose.addOne((place1, Door()))
        }
        if (
          (!rooms.exists(r => {
            r.getAllTiles.contains(Vec2(place2.x, place2.y + 1))
          })  && !rooms
            .exists(r2 =>
              r2.getAllTiles.contains(Vec2(place2.x, place2.y - 1))
            ))  ||
            (!rooms.exists(r => {
            r.getAllTiles.contains(Vec2(place2.x + 1, place2.y))
          }) && !rooms
              .exists(r2 =>
                r2.getAllTiles.contains(Vec2(place2.x - 1, place2.y))
              ))
        ) {
          locPurpose.addOne((place2, Door()))
        }
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
    level.dimensions = dimensions
    level.terrains =
      Array.fill(dimensions * dimensions)((Emptiness(), Trog.pickTileNum))
    rooms.foreach(r => {
      r.getAllTiles.foreach(t => {
        if (
          locPurpose.contains(Vec2(t.x, t.y)) && locPurpose(Vec2(t.x, t.y))
            .isInstanceOf[Door]
        ) {
            level.terrains((t.y * dimensions) + t.x) =
              (ClosedDoor(), Trog.pickTileNum)
        } else
          level.terrains((t.y * dimensions) + t.x) = (Floor(), Trog.pickTileNum)
      })
    })
    level.terrains.zipWithIndex.collect({
      case ((nothing: Emptiness, n: Int), i: Int) => {
        if (
          level
            .adjs(i)
            .exists(l =>
              level
                .terrains(l)
                ._1
                .walkable && !level.terrains(l)._1.isInstanceOf[Emptiness]
            )
        ) level.terrains(i) = (Wall(), Trog.pickTileNum)
      }
    })
    val floorIndices = level.terrains.zipWithIndex.collect({
      case ((floor: Floor, n), index) => index
    })
    val randomFloors = Random.shuffle(floorIndices)
    level.terrains.update(randomFloors(0), (LadderDown(), Trog.pickTileNum))
    level.terrains.update(randomFloors(1), (LadderUp(), Trog.pickTileNum))

    level.upLadder = level.terrains.zipWithIndex
      .collect({ case ((t: LadderUp, n: Int), i) =>
        getVec2fromI(i, level)
      })
      .head
    level.downLadder = level.terrains.zipWithIndex
      .collect({ case ((t: LadderDown, n: Int), i) =>
        getVec2fromI(i, level)
      })
      .head
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
  var theme = Lab()
  var downLadder: Vec2 = _
  var upLadder: Vec2 = _
  var dimensions = 0
  var terrains: Array[(Terrain, Int)] = Array.empty
  def adjs(i: Int): List[Int] = List[Int](
    (i - dimensions - 1),
    (i - dimensions),
    (i - dimensions + 1),
    (i - 1),
    (i + 1),
    (i + dimensions - 1),
    (i + dimensions),
    (i + dimensions + 1)
  ).filter(i => i > 0 && i < dimensions * dimensions)
  def draw(batch: PolygonSpriteBatch): Unit = {
    terrains.zipWithIndex.foreach({
      case (t, i) => {
        batch.setColor(Color.WHITE)
        t._1.draw(batch, getVec2fromI(i, this), theme, t._2)
      }
    })
  }
}
