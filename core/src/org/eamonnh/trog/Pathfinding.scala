package org.eamonnh.trog

import org.eamonnh.trog.procgen.Level

import scala.collection.mutable

object Pathfinding {

  def findRaycastPath(start: Vec2, end: Vec2, level: Level): Option[Path] = {
    var dx = end.x - start.x
    var dy = end.y - start.y
    var startLocX = start.x
    var endLocX = end.x
    var startLocY = start.y
    var endLocY = end.y
    var obstructed = false
    var listPaths: Path = Path(List.empty)
    if(start != end) {
      if (Math.abs(dx) >= Math.abs(dy)) {
        for (xCheck <- startLocX to endLocX by Math.signum(dx).toInt) {
          val yCheck = (endLocY - startLocY) * (xCheck - startLocX) / (endLocX - startLocX) + startLocY
          if (!level.terrains.zipWithIndex.exists(t => getVec2fromI(t._2, level) == Vec2(xCheck.floor.toInt, yCheck) && !t._1._1.walkable)) {
            listPaths.list = (Vec2(xCheck.floor.toInt, yCheck) :: listPaths.list.reverse).reverse
          } else obstructed = true
        }
      } else {
        for (yCheck <- startLocY to endLocY by Math.signum(dy).toInt) {
          val xCheck = (endLocX - startLocX) * (yCheck - startLocY) / (endLocY - startLocY) + startLocX
          if (!level.terrains.zipWithIndex.exists(t => getVec2fromI(t._2, level) == Vec2(xCheck, yCheck.floor.toInt) && !t._1._1.walkable)) {
            listPaths.list = (Vec2(xCheck, yCheck.floor.toInt) :: listPaths.list.reverse).reverse
          } else obstructed = true
        }
      }
    } else obstructed = true
    if(!obstructed) Some(listPaths) else None
  }
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
    while (
      !paths.exists(lInt =>
        lInt.list.head.getAdjacents.contains(end) || lInt.list.head == end
      ) && paths.nonEmpty
    ) {
      paths = for {
        path <- paths
        newPath <- path.extendHalfPath(visitedCells, level)
      } yield newPath

    }
    paths.find(path =>
      path.list.head == end || path.list.head.getAdjacents.contains(end)
    )
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

case class Path(var list: List[Vec2]) {
  def extendHalfPath(visCells: mutable.Set[Vec2], level: Level): List[Path] = {
    for {
      loc <- list.head.getHalfAdjacents
      if (visCells.add(loc))
      if (loc.x >= 0 && loc.y >= 0 && loc.x < level.dimensions && loc.y < level.dimensions)
      if (level.terrains(loc.x + (loc.y * level.dimensions))._1.walkable)
    } yield add(loc)

  }

  def extendPath(visCells: mutable.Set[Vec2], level: Level): List[Path] = {
    for {
      loc <- list.head.getAdjacents
      if (visCells.add(loc))
      if (loc.x >= 0 && loc.y >= 0 && loc.x < level.dimensions && loc.y < level.dimensions)
      if (level.terrains(loc.x + (loc.y * level.dimensions))._1.walkable)
    } yield add(loc)

  }

  def add(loc: Vec2): Path = Path(loc :: list)

}
