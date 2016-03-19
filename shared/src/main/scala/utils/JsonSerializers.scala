package utils

import models.plan.PlanNode
import models.{ RequestMessage, ResponseMessage }
import upickle.Js
import upickle.default._

object JsonSerializers {
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
