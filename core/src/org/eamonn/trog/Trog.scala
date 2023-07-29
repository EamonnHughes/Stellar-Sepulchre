package org.eamonn.trog

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import org.eamonn.trog.procgen.World
import org.eamonn.trog.scenes.Home
import org.eamonn.trog.util.{GarbageCan, TextureWrapper}

class Trog extends ApplicationAdapter {
  import Trog.garbage

  private val idMatrix = new Matrix4()
  private var batch: PolygonSpriteBatch = _
  private var scene: Scene = _

  override def create(): Unit = {

    Gdx.input.setCatchKey(Input.Keys.BACK, true)

    batch = garbage.add(new PolygonSpriteBatch())

    Trog.Square = TextureWrapper.load("Square.png")
    Trog.Circle = TextureWrapper.load("Circle.png")

    //    Trog.sound = Trog.loadSound("triangle.mp3")

    Text.loadFonts()

    setScene(new Home(World()))
  }

  override def render(): Unit = {

    val delta = Gdx.graphics.getDeltaTime
    scene.update(delta) foreach setScene
    scene.updateCamera()
    ScreenUtils.clear(0f, 0f, 0f, 1)
    batch.begin()
    batch.getTransformMatrix.setToTranslation(Trog.translationX*screenUnit, Trog.translationY*screenUnit, 0)
    scene.render(batch)
    scene.renderUI(batch)
    batch.end()
  }

  override def dispose(): Unit = {
    garbage.dispose()
  }

  private def setScene(newScene: Scene): Unit = {
    scene = newScene
    Gdx.input.setInputProcessor(scene.init())
  }

}

object Trog {
  implicit val garbage: GarbageCan = new GarbageCan

  var sound: Sound = _
  var Square: TextureWrapper = _
  var Circle: TextureWrapper = _
  var translationX = 0
  var translationY = 0

  def mobile: Boolean = isMobile(Gdx.app.getType)

  private def isMobile(tpe: ApplicationType) =
    tpe == ApplicationType.Android || tpe == ApplicationType.iOS

  private def loadSound(path: String)(implicit garbage: GarbageCan): Sound =
    garbage.add(Gdx.audio.newSound(Gdx.files.internal(path)))

}
