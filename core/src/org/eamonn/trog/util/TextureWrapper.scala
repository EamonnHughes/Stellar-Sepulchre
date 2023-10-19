package org.eamonn.trog
package util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Pixmap, Texture}
import com.badlogic.gdx.utils.Disposable

import scala.collection.mutable

class TextureWrapper(val pixmap: Pixmap) extends Disposable {

  val width: Int = pixmap.getWidth
  val height: Int = pixmap.getHeight
  val texture = new Texture(pixmap)

  override def dispose(): Unit = {
    texture.dispose()
    pixmap.dispose()
  }

}

object TextureWrapper {

  val textureCache = mutable.Map.empty[String, TextureWrapper]

  def load(path: String)(implicit garbageCan: GarbageCan): TextureWrapper = {
    textureCache.getOrElseUpdate(
      path, {
        val fileHandle = Gdx.files.internal(path)
        val pixmap = new Pixmap(fileHandle)
        garbageCan.add(new TextureWrapper(pixmap))
      }
    )
  }

  implicit def toTexture(wrapper: TextureWrapper): Texture = wrapper.texture

}
