package org.eamonn.trog.character

trait Stats {

  var ac: Int
  var exp: Int
  var nextExp: Int
  var maxHealth: Int
  var health: Int
  var sightRad: Int
  var level: Int
  var damageMod: Float
  var attackMod: Float
  var critChance: Int
  var critMod: Float
  var skills: List[Skill]
}

case class basePlayerStats() extends Stats {
  var ac = 5
  var exp = 0
  var nextExp = 50
  var maxHealth = 10
  var health = 0
  var sightRad = 6
  var level = 1
  var damageMod = 0
  var attackMod = 0f
  var critChance = 5
  var critMod = 2f
  override var skills: List[Skill] = List(throwRock())
}

case class makeStats(
    mAc: Int,
    mExp: Int,
    mNExp: Int,
    mMHeal: Int,
    mHeal: Int,
    mSrad: Int,
    mLev: Int,
    mDmg: Int,
    mAtk: Float,
    mCrc: Int,
    mCrm: Float
) extends Stats {
  var ac: Int = mAc
  var exp: Int = mExp
  var nextExp: Int = mNExp
  var maxHealth: Int = mMHeal
  var health: Int = mHeal
  var sightRad: Int = mSrad
  var level: Int = mLev
  var damageMod: Float = mDmg
  var attackMod: Float = mAtk
  var critChance: Int = mCrc
  var critMod: Float = mCrm
  override var skills: List[Skill] = List.empty
}
