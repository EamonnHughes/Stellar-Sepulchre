package org.eamonnh.trog

import com.badlogic.gdx.Application.ApplicationType
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Cursor.SystemCursor
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import org.eamonnh.trog.character.Player
import org.eamonnh.trog.procgen.{Level, Theme, World}
import org.eamonnh.trog.scenes.{Game, Home}
import org.eamonnh.trog.util.{GarbageCan, TextureWrapper}

class Trog extends ApplicationAdapter {

  import Trog.garbage

  private val idMatrix = new Matrix4()
  private var batch: PolygonSpriteBatch = _
  private var scene: Scene = _

  override def create(): Unit = {

    Gdx.input.setCatchKey(Input.Keys.BACK, true)

    batch = garbage.add(new PolygonSpriteBatch())

    Text.loadFonts()
    var home: Home = new Home(World())
    if (SaveLoad.getSaveFile(0).exists()) {
      home.world = SaveLoad.loadState(0).world
    } else {
      SaveLoad.saveState(new Game(new Level, new Player, home.world), 0)
    }
    home.game = SaveLoad.loadState(0)
    setScene(home)
  }

  override def render(): Unit = {

    val delta = Gdx.graphics.getDeltaTime
    scene.update(delta) foreach setScene
    scene.updateCamera()
    ScreenUtils.clear(0f, 0f, 0f, 1)
    batch.begin()
    batch.getTransformMatrix.setToTranslation(
      Trog.translationX * screenUnit,
      Trog.translationY * screenUnit,
      0
    )
    scene.render(batch)
    scene.renderUI(batch)
    batch.end()
  }

  private def setScene(newScene: Scene): Unit = {
    scene = newScene
    Gdx.input.setInputProcessor(scene.init())
  }

  override def dispose(): Unit = {
    garbage.dispose()
  }

}

object Trog {
  implicit val garbage: GarbageCan = new GarbageCan

  lazy val inGameOST: Sound = Trog.loadSound("AmbienceDepths.mp3")
  lazy val Tolling: Sound = Trog.loadSound("Toll.mp3")
  lazy val Jingle: Sound = Trog.loadSound("jingle.mp3")
  lazy val Crunch: Sound = Trog.loadSound("crunch.mp3")
  lazy val Square: TextureWrapper = TextureWrapper.load("Square.png")
  lazy val Highlight: TextureWrapper = TextureWrapper.load("Highlight.png")
  lazy val homeBG: TextureWrapper = TextureWrapper.load("sepulctbg.png")
  lazy val loadBG: TextureWrapper = TextureWrapper.load("generate.png")
  lazy val asleep: TextureWrapper = TextureWrapper.load("asleep.png")
  lazy val UICornerLeft: TextureWrapper =
    TextureWrapper.load("UICornerLeft.png")
  lazy val UICornerRight: TextureWrapper =
    TextureWrapper.load("UICornerRight.png")
  lazy val UIHotbar: TextureWrapper = TextureWrapper.load("UIHotbar.png")
  lazy val UIHealthBarFrame: TextureWrapper =
    TextureWrapper.load("UIHealthBarFrame.png")
  lazy val UIXPBarFrame: TextureWrapper =
    TextureWrapper.load("UIXPBarFrame.png")
  lazy val EffectSplash: TextureWrapper =
    TextureWrapper.load("EffectSplash.png")
  var translationX = 0
  var translationY = 0

  def mkTileImage(kind: String, theme: Theme, number: Number): TextureWrapper =
    TextureWrapper.load(kind + theme.stringName + number + ".png")

  def pickTileNum: Int = {
    if (Math.random() < .925) 1
    else if (Math.random() < .75) 2
    else if (Math.random() < .33) 3
    else if (Math.random() < .5) 4
    else 5
  }

  def mobile: Boolean = isMobile(Gdx.app.getType)

  private def isMobile(tpe: ApplicationType) =
    tpe == ApplicationType.Android || tpe == ApplicationType.iOS

  private def loadSound(path: String)(implicit garbage: GarbageCan): Sound =
    garbage.add(Gdx.audio.newSound(Gdx.files.internal(path)))

}
