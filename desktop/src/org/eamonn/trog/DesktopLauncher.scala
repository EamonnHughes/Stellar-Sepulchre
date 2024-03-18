package org.eamonnh.trog

import com.badlogic.gdx.backends.lwjgl3.{
  Lwjgl3Application,
  Lwjgl3ApplicationConfiguration
}

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
object DesktopLauncher extends App {
  var dm = Lwjgl3ApplicationConfiguration.getDisplayMode()
  val config = new Lwjgl3ApplicationConfiguration
  config.setForegroundFPS(60)
  var size = ((dm.width min dm.height) /20/32)*20*32
  config.setWindowedMode(size, size)
  new Lwjgl3Application(new Trog, config)
}
