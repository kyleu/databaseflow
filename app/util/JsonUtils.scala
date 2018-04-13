package util

import io.circe.Json

object JsonUtils {
  def toString(v: Json): String = v match {
    case s if s.isString => s.asString.get
    case n if n.isNumber => n.asNumber.get.toDouble.toString
    case a if a.isArray => "[" + a.asArray.get.map(v => toString(v)).mkString(", ") + "]"
    case b if b.isBoolean => b.asBoolean.get.toString
    case o if o.isObject => "{" + toStringMap(o.asObject.get.toMap).map(x => s"${x._1}: ${x._2}").mkString(", ") + "}"
    case x => throw new IllegalStateException(s"Invalid param type [${x.getClass.getName}].")
  }

  def toStringMap(params: Map[String, Json]): Map[String, String] = params.map(p => p._1 -> toString(p._2))
}
