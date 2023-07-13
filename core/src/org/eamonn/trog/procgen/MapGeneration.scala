package org.eamonn.trog.procgen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Trog.garbage
import org.eamonn.trog.procgen.MapGeneration.tiles
import org.eamonn.trog.screenUnit
import org.eamonn.trog.util.TextureWrapper

import scala.util.Random

object MapGeneration {

  val tileZero = tileType(
    down = "000",
    left = "000",
    right = "000",
    up = "000",
    texture = TextureWrapper.load("dungeontiles/0.png")
  )
  val tileOne = tileType(
    down = "101",
    left = "111",
    right = "111",
    up = "101",
    texture = TextureWrapper.load("dungeontiles/1.png")
  )
  val tileTwo = tileType(
    down = "111",
    left = "111",
    right = "111",
    up = "111",
    texture = TextureWrapper.load("dungeontiles/2.png")
  )
  val tileThree = tileType(
    down = "101",
    left = "100",
    right = "001",
    up = "000",
    texture = TextureWrapper.load("dungeontiles/3.png")
  )
  val tileFour = tileType(
    down = "001",
    left = "111",
    right = "100",
    up = "111",
    texture = TextureWrapper.load("dungeontiles/4.png")
  )
  val tileFive = tileType(
    down = "001",
    left = "111",
    right = "000",
    up = "100",
    texture = TextureWrapper.load("dungeontiles/5.png")
  )

  val tiles: List[tileType] = List(
    tileZero,
    tileOne,
    tileTwo,
    tileThree,
    tileFour,
    tileFive,
    tileOne.rotateTile(1),
    tileThree.rotateTile(1),
    tileThree.rotateTile(2),
    tileThree.rotateTile(3),
    tileFour.rotateTile(1),
    tileFour.rotateTile(2),
    tileFour.rotateTile(3),
    tileFive.rotateTile(1),
    tileFive.rotateTile(2),
    tileFive.rotateTile(3),
  )

  def generateNewGrid(genMap: GeneratedMap): Array[gridItem] = {
    var nextGrid: Array[gridItem] = genMap.grid.clone()
    for (x <- 0 until genMap.dimensions) {
      for (y <- 0 until genMap.dimensions) {
        var index = x + y * genMap.dimensions
        if (genMap.grid(index).collapsed) {
          nextGrid(index) = genMap.grid(index)
        } else {
          var availOptions = getAllowed(x, y, genMap)
          if (availOptions.nonEmpty) {
            nextGrid(index).options = availOptions
            if (availOptions.length == 1) nextGrid(index).collapsed = true
          } else {
            genMap.grid = Array.fill(genMap.dimensions * genMap.dimensions)(
              gridItem(collapsed = false, getAllTileNumbers)
            )
          }
        }
      }
    }
    nextGrid
  }

  def getAllowed(x: Int, y: Int, genMap: GeneratedMap): List[Int] = {
    var index = x + y * genMap.dimensions

    var availOptions = genMap.grid(index).options

    //look down
    if (y > 0) {
      var downTile = genMap.grid(index - genMap.dimensions)
      availOptions = availOptions.filter(optionAvailable =>
        downTile.options.exists(optionDown =>
          tiles(optionDown).up == tiles(optionAvailable).down.reverse
        )
      )
    } else {
      availOptions = List(availOptions(Random.nextInt(availOptions.length)))
    }
    //look left
    if (x > 0) {
      var leftTile = genMap.grid(index - 1)
      availOptions = availOptions.filter(optionAvailable =>
        leftTile.options.exists(optionLeft =>
          tiles(optionLeft).right == tiles(optionAvailable).left.reverse
        )
      )
    } else {
      availOptions = List(availOptions(Random.nextInt(availOptions.length)))
    }
    //look right
    if (x < genMap.dimensions - 1) {
      var rightTile = genMap.grid(index + 1)
      availOptions = availOptions.filter(optionAvailable =>
        rightTile.options.exists(optionRight =>
          tiles(optionRight).left == tiles(optionAvailable).right.reverse
        )
      )

    } else {
      availOptions = List(availOptions(Random.nextInt(availOptions.length)))
    }
    //look up
    if (y < genMap.dimensions - 1) {
      var upTile = genMap.grid(index + genMap.dimensions)
      availOptions = availOptions.filter(optionAvailable =>
        upTile.options.exists(optionUp =>
          tiles(optionUp).down == tiles(optionAvailable).up.reverse
        )
      )
    } else {
      availOptions = List(availOptions(Random.nextInt(availOptions.length)))
    }
    availOptions
  }

  def collapseLeast(genMap: GeneratedMap): Unit = {
    if (genMap.grid.exists(_.collapsed == false)) {
      val leastLength =
        genMap.grid.filter(_.collapsed == false).map(_.options.length).min
      val least = genMap.grid.filter(cell =>
        cell.options.length == leastLength && !cell.collapsed
      )

      val picked = least(Random.nextInt(least.length))

      var x = genMap.grid.indexOf(picked) % genMap.dimensions
      var y = (genMap.grid.indexOf(picked) - x) / genMap.dimensions
      var allowed = getAllowed(x, y, genMap)
      if (allowed.isEmpty) {
        genMap.grid = Array.fill(genMap.dimensions * genMap.dimensions)(
          gridItem(collapsed = false, getAllTileNumbers)
        )
      } else {
        picked.options = List(
          allowed(Random.nextInt(allowed.length))
        )
        picked.collapsed = true

        genMap.grid = generateNewGrid(genMap)
      }
    }
  }
  def getAllTileNumbers: List[Int] = {
    var tileNums = List.empty[Int]
    for (i <- tiles.indices) {
      tileNums = i :: tileNums
    }
    tileNums
  }
}

case class GeneratedMap(dimensions: Int) {

  def draw(batch: PolygonSpriteBatch): Unit = {
    for (x <- 0 until dimensions) {
      for (y <- 0 until dimensions) {
        var cell = grid(x + y * dimensions)
        batch.setColor(Color.WHITE)
        var index = cell.options.head

        batch.draw(
          tiles(index).texture,
          x * screenUnit,
          y * screenUnit,
          screenUnit / 2,
          screenUnit / 2,
          screenUnit,
          screenUnit,
          1f,
          1f,
          tiles(index).rot,
          0,
          0,
          27,
          27,
          false,
          false
        )
      }
    }

  }
  var grid: Array[gridItem] = _
  def generate(): Unit = {
    grid = Array.fill(dimensions*dimensions)(
      gridItem(collapsed = false, MapGeneration.getAllTileNumbers)
    )
    while (grid.exists(item => !item.collapsed)) {
      MapGeneration.collapseLeast(this)
    }

  }

}

case class gridItem(
    var collapsed: Boolean = false,
    var options: List[Int] = MapGeneration.getAllTileNumbers
)

case class tileType(
    var down: String,
    var left: String,
    var right: String,
    var up: String,
    var rot: Float = 0f,
    var texture: TextureWrapper
) {

  def rotateTile(rotation: Int): tileType = {
    if (rotation == 1) {
      return tileType(
        texture = texture,
        down = right,
        left = down,
        up = left,
        right = up,
        rot = -90
      )
    } else if (rotation == 2) {
      return tileType(
        texture = texture,
        down = up,
        left = right,
        up = down,
        right = left,
        rot = -180
      )
    } else if (rotation == 3) {
      return tileType(
        texture = texture,
        down = left,
        left = up,
        up = right,
        right = down,
        rot = -270
      )
    } else return this
  }
}
