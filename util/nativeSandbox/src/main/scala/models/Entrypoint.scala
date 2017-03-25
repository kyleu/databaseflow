package models

import models.test.TestModel

object Entrypoint {
  def main(args: Array[String]): Unit = {
    println("Hello, native!")
    println(TestModel.values.head.key)
  }
}
