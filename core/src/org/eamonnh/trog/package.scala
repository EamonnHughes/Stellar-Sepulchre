package org.eamonnh

import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.Input.Peripheral
import com.badlogic.gdx.graphics.Color
import org.eamonnh.trog.procgen.Level

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

// Things kinda stolen from scaloi
package object trog {

  val CenterAlign = 1

  def d(die: Int): Int = Random.nextInt(die) + 1

  def getVec2fromI(i: Int, level: Level): Vec2 =
    Vec2(i % level.dimensions, (i - (i % level.dimensions)) / level.dimensions)
  def getIfromVec2(v: Vec2, level: Level): Int = (v.y * level.dimensions) + v.x

  def d(nOd: Int, die: Int): Int = {
    var amt = 0
    for (i <- 0 until (nOd)) {
      amt += Random.nextInt(die) + 1
    }
    amt
  }

  // 16 should match desktop launcher
  def screenUnit: Float = (Geometry.ScreenWidth min Geometry.ScreenHeight) / 20

  def compassAvailable: Boolean =
    input.isPeripheralAvailable(Peripheral.Compass)

  implicit class AnyOps(val self: Any) extends AnyVal {

    /** Replace this value with [a]. */
    def as[A](a: A): A = a
  }

  implicit class FloatOps(val self: Float) extends AnyVal {

    /** Clamp this value between 0f and 1f inclusive. */
    def clamp: Float = clamp(1f)

    /** Clamp this value between 0f and [max] inclusive. */
    def clamp(max: Float): Float =
      if (self < 0f) 0f else if (self > max) max else self

    /** Increases an alpha by [delta] time interval spread over [seconds] seconds limited to 1f. */
    def alphaUp(delta: Float, seconds: Float): Float =
      (self + delta / seconds) min 1f

    /** Decreases an alpha by [delta] time interval spread over [seconds] seconds limited to 0f. */
    def alphaDown(delta: Float, seconds: Float): Float =
      (self - delta / seconds) max 0f
  }

  implicit class BooleanOps(val self: Boolean) extends AnyVal {
    def option[A](a: => A): Option[A] = if (self) Some(a) else None

    def fold[A](ifTrue: => A, ifFalse: => A): A = if (self) ifTrue else ifFalse
  }

  implicit class FiniteDurationOps(val self: FiniteDuration) extends AnyVal {
    def toHumanString: String = {
      largestUnit.fold("no time at all") { u =>
        val scaled = toFiniteDuration(u)
        scaled.toString
        val v = TimeUnit.values.apply(u.ordinal - 1)
        val modulus = FiniteDuration(1, u).toUnit(v).toInt
        val remainder = self.toUnit(v).toLong % modulus
        if (remainder > 0)
          scaled.toString + ", " + FiniteDuration(remainder, v)
        else
          scaled.toString
      }
    }

    def toFiniteDuration(tu: TimeUnit): FiniteDuration =
      FiniteDuration(self.toUnit(tu).toLong, tu)

    protected def largestUnit: Option[TimeUnit] =
      TimeUnit.values.findLast(u => self.toUnit(u) >= 1.0)
  }

  implicit class OptionOps[A](val self: Option[A]) extends AnyVal {
    def isTrue(implicit Booleate: Booleate[A]): Boolean =
      self.fold(false)(Booleate.value)

    def isFalse(implicit Booleate: Booleate[A]): Boolean =
      self.fold(false)(Booleate.unvalue)
  }

  private trait Booleate[A] {
    def value(a: A): Boolean

    final def unvalue(a: A): Boolean = !value(a)
  }

  case class Vec2(var x: Int, var y: Int) {
    def getHalfAdjacents: List[Vec2] = {
      List(
        Vec2(x, y - 1),
        Vec2(x, y + 1),
        Vec2(x - 1, y),
        Vec2(x + 1, y)
        //   Vec2(x - 1, y - 1),
        //    Vec2(x - 1, y + 1),
        //     Vec2(x + 1, y - 1),
        //      Vec2(x + 1, y + 1)
      )
    }

    def getAdjacents: List[Vec2] = {
      List(
        Vec2(x, y - 1),
        Vec2(x, y + 1),
        Vec2(x - 1, y),
        Vec2(x + 1, y),
        Vec2(x - 1, y - 1),
        Vec2(x - 1, y + 1),
        Vec2(x + 1, y - 1),
        Vec2(x + 1, y + 1)
      )
    }
  }

  implicit class ColorOps(val self: Color) extends AnyVal {

    /** Returns a new colour with alpha set to [alpha]. */
    def withAlpha(alpha: Float): Color =
      new Color(self.r, self.g, self.b, alpha)

    /** Returns a new colour with alpha multiplied by [alpha]. */
    def ⍺(alpha: Float): Color =
      new Color(self.r, self.g, self.b, self.a * alpha)

    /** Returns a new colour with alpha multiplied by [alpha]². */
    def ⍺⍺(alpha: Float): Color =
      new Color(self.r, self.g, self.b, self.a * alpha * alpha)
  }

  private object Booleate {
    implicit def booleate: Booleate[Boolean] = b => b
  }
}
