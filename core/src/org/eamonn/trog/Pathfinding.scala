package org.eamonn.trog

import org.eamonn.trog.procgen.{Connection, GeneratedMap, Level}

import scala.collection.mutable
object Pathfinding {
  def findHalfPath(start: Vec2, end: Vec2, level: Level): Option[Path] = {
    val visitedCells = mutable.Set.empty[Vec2]
    var paths = List(Path(List(start)))
    while (!paths.exists(lInt => lInt.list.head == end) && paths.nonEmpty) {
      paths = for {
        path <- paths
        newPath <- path.extendHalfPath(visitedCells, level)
      } yield newPath

    }
    paths.find(path => path.list.head == end)
  }

  def findPathUpto(start: Vec2, end: Vec2, level: Level): Option[Path] = {
    val visitedCells = mutable.Set.empty[Vec2]
    var paths = List(Path(List(start)))
    while (!paths.exists(lInt => lInt.list.head.getAdjacents.contains(end) || lInt.list.head == end) && paths.nonEmpty) {
      paths = for {
        path <- paths
        newPath <- path.extendHalfPath(visitedCells, level)
      } yield newPath

    }
    paths.find(path => path.list.head == end || path.list.head.getAdjacents.contains(end))
  }

  def findPath(start: Vec2, end: Vec2, level: Level): Option[Path] = {
    val visitedCells = mutable.Set.empty[Vec2]
    var paths = List(Path(List(start)))
    while (!paths.exists(lInt => lInt.list.head == end) && paths.nonEmpty) {
      paths = for {
        path <- paths
        newPath <- path.extendPath(visitedCells, level)
      } yield newPath

    }
    paths.find(path => path.list.head == end)
  }
}

case class connectPath(list: List[Connection]) {
  def extendPath(alreadyUsed: mutable.Set[Connection], genMap: GeneratedMap): List[connectPath] = {
    for{
      con <- list.head.getConnected(genMap.connections)
      if(alreadyUsed.add(con))
    } yield add(con)
  }

  def add(con: Connection): connectPath = connectPath(con :: list)
}


case class Path(list: List[Vec2]) {
  def extendHalfPath(visCells: mutable.Set[Vec2], level: Level): List[Path] = {
    for {
      loc <- list.head.getHalfAdjacents
      if (visCells.add(loc))
      if(level.walkables.exists(l => l.x == loc.x && l.y == loc.y))
    } yield add(loc)

  }

  def extendPath(visCells: mutable.Set[Vec2], level: Level): List[Path] = {
    for {
      loc <- list.head.getAdjacents
      if (visCells.add(loc))
      if (level.walkables.exists(l => l.x == loc.x && l.y == loc.y))
    } yield add(loc)

  }

  def add(loc: Vec2): Path = Path(loc :: list)

}