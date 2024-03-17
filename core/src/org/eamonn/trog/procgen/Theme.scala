package org.eamonn.trog.procgen

trait Theme {
  val stringName: String
}
case class Lab() extends Theme {
  val stringName = "lab"
}