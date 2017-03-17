package models.test

sealed abstract class TestModel(val key: String)

object TestModel {
  case object TestOne extends TestModel("one")
  case object TestTwo extends TestModel("two")
  case object TestThree extends TestModel("three")

  val values = Seq(TestOne, TestTwo, TestThree)
}
