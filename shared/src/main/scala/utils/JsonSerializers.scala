package utils

import java.util.UUID

import models.plan.PlanNode
import models.{ RequestMessage, ResponseMessage }
import upickle.Js
import upickle.default._

object JsonSerializers {
  // Options
  implicit val stringOptionReader = Reader[Option[String]] {
    case Js.Str(s) => Some(s)
    case _ => None
  }
  implicit val stringOptionWriter = Writer[Option[String]] {
    case Some(s) => Js.Str(s)
    case None => Js.Null
  }

  implicit val intOptionReader = Reader[Option[Int]] {
    case Js.Num(i) => Some(i.toInt)
    case _ => None
  }
  implicit val intOptionWriter = Writer[Option[Int]] {
    case Some(i) => Js.Num(i.toDouble)
    case None => Js.Null
  }

  implicit val uuidOptionReader = Reader[Option[UUID]] {
    case Js.Str(uuid) => Some(UUID.fromString(uuid))
    case _ => None
  }
  implicit val uuidOptionWriter = Writer[Option[UUID]] {
    case Some(uuid) => Js.Str(uuid.toString)
    case None => Js.Null
  }

  implicit val boolOptionReader = Reader[Option[Boolean]] {
    case Js.True => Some(true)
    case Js.False => Some(false)
    case _ => None
  }
  implicit val boolOptionWriter = Writer[Option[Boolean]] {
    case Some(b) if b => Js.True
    case Some(b) => Js.False
    case None => Js.Null
  }

  // Recursive structures
  private[this] def readPlanNode(x: Js.Value): PlanNode = {
    PlanNode(
      title = readJs[String](x("title")),
      costs = readJs[PlanNode.Costs](x("costs")),
      properties = readJs[Map[String, String]](x("properties")),
      tags = readJs[Seq[String]](x("tags")),
      children = x("children") match {
        case a: Js.Arr => a.value.map(n => readPlanNode(n.asInstanceOf[Js.Obj]))
        case other => throw new IllegalArgumentException(other.toString)
      }
    )
  }
  implicit val planNodeReader = Reader[PlanNode] {
    case x: Js.Obj => readPlanNode(x)
  }

  private[this] def writePlanNode(node: PlanNode): Js.Value = {
    Js.Obj(
      "title" -> writeJs(node.title),
      "costs" -> writeJs(node.costs),
      "properties" -> writeJs(node.properties),
      "tags" -> writeJs(node.tags),
      "children" -> Js.Arr(node.children.map(writePlanNode): _*)
    )
  }
  implicit val planNodeWriter = Writer[PlanNode] {
    case x => writePlanNode(x)
  }

  // Wire messages
  def readRequestMessage(json: Js.Value) = readJs[RequestMessage](json)
  def writeRequestMessage(rm: RequestMessage, debug: Boolean = false) = if (debug) {
    write(rm, indent = 2)
  } else {
    write(rm)
  }

  def readResponseMessage(json: String) = read[ResponseMessage](json)
  def writeResponseMessage(rm: ResponseMessage) = writeJs(rm)
}
