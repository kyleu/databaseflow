import upickle._
import upickle.legacy._

object JsonSerializers {
  implicit val stringOptionWriter = Writer[Option[String]] {
    case Some(s) => Js.Str(s)
    case None => Js.Null
  }
  implicit val intOptionWriter = Writer[Option[Int]] {
    case Some(i) => Js.Num(i)
    case None => Js.Null
  }
  implicit val boolOptionWriter = Writer[Option[Boolean]] {
    case Some(b) => if (b) { Js.True } else { Js.False }
    case None => Js.Null
  }

  def write(j: Js.Value) = json.write(j)
}
