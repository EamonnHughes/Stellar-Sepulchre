package org.eamonn.trog

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import org.eamonn.trog.character.Player
import org.eamonn.trog.procgen.{Level, World}
import org.eamonn.trog.scenes.Game

import java.io._

object SaveLoad {
  def mainDir = "interstellargrave/"

  def SaveDir = s"${mainDir}saves/"

  def saveState(game: Game, slot: Int): Unit = {
    game.keysDown = List.empty
    game.clicked = false
    val saveFile: FileHandle = getSaveFile(slot)
    println(saveFile.file())
    if (!saveFile.exists()) saveFile.file().createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(saveFile.file()))
    oos.writeObject(game)
    oos.close()
  }

  def loadState(slot: Int): Game = {
    val saveFile: FileHandle = getSaveFile(slot)
    var game: Game = new Game(new Level, new Player, new World)
    try {
      val ois = new ObjectInputStream(new FileInputStream(saveFile.file()))
      game = ois.readObject().asInstanceOf[Game]
      ois.close()
    } catch {
      case _: Throwable => {
        saveFile.delete()
        SaveLoad.saveState(game, slot)
      }
    }
    game
  }

  def getSaveFile(slot: Int): FileHandle = {
    val fileHandle: FileHandle = Gdx.files.external(SaveDir + slot)
    var parentDir: File = fileHandle.file().getParentFile
    parentDir.mkdirs()
    fileHandle
  }
}
