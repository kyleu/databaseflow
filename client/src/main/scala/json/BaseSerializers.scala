package json

import java.util.UUID

import upickle._
import upickle.legacy._

object BaseSerializers {
  implicit val stringOptionWriter = Writer[Option[String]] {
    case Some(s) => Js.Str(s)
    case None => Js.Null
  }
  implicit val stringOptionReader = Reader[Option[String]] {
    case Js.Str(s) => Some(s)
    case _ => None
  }

  implicit val intOptionWriter = Writer[Option[Int]] {
    case Some(i) => Js.Num(i.toDouble)
    case None => Js.Null
  }
  implicit val intOptionReader = Reader[Option[Int]] {
    case Js.Num(i) => Some(i.toInt)
    case _ => None
  }

  implicit val uuidOptionWriter = Writer[Option[UUID]] {
    case Some(uuid) => Js.Str(uuid.toString)
    case None => Js.Null
  }
  implicit val uuidOptionReader = Reader[Option[UUID]] {
    case Js.Str(uuid) => Some(UUID.fromString(uuid))
    case _ => None
  }

  implicit val boolOptionWriter = Writer[Option[Boolean]] {
    case Some(b) => if (b) { Js.True } else { Js.False }
    case None => Js.Null
  }
  implicit val boolOptionReader = Reader[Option[Boolean]] {
    case Js.True => Some(true)
    case Js.False => Some(false)
    case _ => None
  }

  def write(j: Js.Value) = json.write(j)
  def read(s: String) = json.read(s)
}
