package org.eamonn.trog
package scenes

import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import org.eamonn.trog.Scene
import org.eamonn.trog.procgen.{GeneratedMap, Level}

import scala.util.Random

class LevelGen(player: Player) extends Scene {
  var cameraLocation: Vec2 = Vec2(0, 0)
  var genMap = GeneratedMap(45, 6, 10, .25f)
  var doneGenerating = false
  var level = new Level

  override def init(): InputAdapter = {
    new LevelGenControl(this)
  }
  override def update(delta: Float): Option[Scene] = {
    if (!doneGenerating) { doneGenerating = genMap.generate() } else {
      if(level.walkables.isEmpty) {
        level = genMap.doExport()
        player.location = level.walkables(Random.nextInt(level.walkables.length)).copy()
      }
    }
    if(doneGenerating && level.walkables.nonEmpty) Some(new Game(level, player)) else None
  }

  override def render(batch: PolygonSpriteBatch): Unit = {
    Text.mediumFont.setColor(Color.WHITE)
    Text.mediumFont.draw(batch, "Generating Floor", Geometry.ScreenWidth/2, Geometry.ScreenHeight)
  }
}
class LevelGenControl(gen: LevelGen) extends InputAdapter {
}
