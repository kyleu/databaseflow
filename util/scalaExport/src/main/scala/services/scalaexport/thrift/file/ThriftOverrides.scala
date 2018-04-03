package services.scalaexport.thrift.file

import better.files._

class ThriftOverrides(root: String = "tmp/thrift") {
  def overrideFor(cls: String, field: String) = overrides.find(o => o.cls == cls && o.field == field)

  case class ThriftOverride(cls: String, field: String, fromThrift: String = "", asThrift: String = "")

  lazy val overrides = {
    val f = File(s"$root/overrides.txt")
    if (f.exists) {
      val ret = collection.mutable.ArrayBuffer.empty[ThriftOverride]
      var current: Option[ThriftOverride] = None
      f.lines.toSeq.foreach {
        case line if line.trim.isEmpty => // noop
        case line if line.startsWith("[") => line.drop(1).dropRight(1).split("::").toList match {
          case cls :: field :: Nil => current match {
            case Some(c) if c.fromThrift.nonEmpty && c.asThrift.nonEmpty =>
              ret += c
              current = Some(ThriftOverride(cls, field))
            case Some(_) => throw new IllegalStateException(s"Line $line encountered with unfinished active override [$current].")
            case None => current = Some(ThriftOverride(cls, field))
          }
          case _ => throw new IllegalStateException(s"Unhandled header line [$line].")
        }
        case line if current.exists(_.fromThrift.isEmpty) => current = current.map(_.copy(fromThrift = line))
        case line if current.exists(_.asThrift.isEmpty) => current = current.map(_.copy(asThrift = line))
        case line => throw new IllegalStateException(s"Too many parameters for line [$line].")
      }
      current.foreach {
        case c if c.fromThrift.nonEmpty && c.asThrift.nonEmpty => ret += c
        case c => throw new IllegalStateException(s"Incomplete override [$c].")
      }
      //println(s"[${ret.size}] overrides available.")
      ret.toIndexedSeq
    } else {
      //println("No overrides available.")
      Nil
    }
  }

  lazy val imports = {
    val f = File(s"$root/imports.txt")
    if (f.exists) {
      val ret = collection.mutable.HashMap.empty[String, Seq[(String, String)]]
      var current = ""
      f.lines.toSeq.foreach {
        case line if line.trim.isEmpty => // noop
        case line if line.startsWith("[") => current = line.drop(1).dropRight(1)
        case line => ret(current) = ret.getOrElseUpdate(current, Nil) :+ (line.substring(0, line.lastIndexOf(".")) -> line.substring(line.lastIndexOf(".") + 1))
      }
      //println(s"[${ret.size}] imports available.")
      ret.toMap
    } else {
      //println("No imports available.")
      Map.empty[String, Seq[(String, String)]]
    }
  }
}
