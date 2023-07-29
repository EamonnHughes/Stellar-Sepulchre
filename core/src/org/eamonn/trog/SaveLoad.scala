package org.eamonn.trog

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import org.eamonn.trog.procgen.World
import org.eamonn.trog.scenes.Game

import java.io.{
  File,
  FileInputStream,
  FileOutputStream,
  ObjectInputStream,
  ObjectOutputStream
}

object SaveLoad {
  def mainDir = "sepultus/"
  def SaveDir(world: World) = s"${mainDir}saves/${world.name}/"
  def SaveDir(world: String) = s"${mainDir}saves/${world}/"

  def saveState(game: Game, slot: Int): Unit = {
    game.keysDown = List.empty
    game.clicked = false
    val saveFile: FileHandle = getSaveFile(game.world, slot)
    println(saveFile.file())
    if (!saveFile.exists()) saveFile.file().createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(saveFile.file()))
    oos.writeObject(game)
    oos.close()
  }
  def worldList: List[String] = {
    var fileHandle = Gdx.files.external(mainDir+"saves/")
    var l = List.empty[String]
    fileHandle.list().foreach(q => l = fileHandle.file().list().toList)
    l
  }

  def loadState(slot: Int, world: World): Game = {
    val saveFile: FileHandle = getSaveFile(world, slot)
    val ois = new ObjectInputStream(new FileInputStream(saveFile.file()))
    val game = ois.readObject().asInstanceOf[Game]
    ois.close()
    game
  }

  def loadState(slot: Int, world: String): Game = {
    val saveFile: FileHandle = getSaveFile(world, slot)
    val ois = new ObjectInputStream(new FileInputStream(saveFile.file()))
    val game = ois.readObject().asInstanceOf[Game]
    ois.close()
    game
  }

  def getSaveFile(world: World, slot: Int): FileHandle = {
    val fileHandle: FileHandle = Gdx.files.external(SaveDir(world) + slot)
    var parentDir: File = fileHandle.file().getParentFile
    parentDir.mkdirs()
    fileHandle
  }

  def getSaveFile(world: String, slot: Int): FileHandle = {
    val fileHandle: FileHandle = Gdx.files.external(SaveDir(world) + slot)
    var parentDir: File = fileHandle.file().getParentFile
    parentDir.mkdirs()
    fileHandle
  }
}
