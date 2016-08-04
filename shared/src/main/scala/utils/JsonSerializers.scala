package utils

import java.util.UUID

import enumeratum.UPickler
import models.plan.PlanNode
import models.schema.ColumnType
import models.template.Theme
import models.{RequestMessage, ResponseMessage}
import upickle.Js
import upickle.default._

object JsonSerializers {
  // Enumerations
  private[this] implicit val columnTypeReader = UPickler.reader(ColumnType)
  private[this] implicit val columnTypeWriter = UPickler.writer(ColumnType)

  implicit val themeReader = UPickler.reader(Theme)
  implicit val themeWriter = UPickler.writer(Theme)

  // Recursive structures
  private[this] def readPlanNode(x: Js.Value): PlanNode = PlanNode(
    id = readJs[UUID](x("id")),
    title = readJs[String](x("title")),
    nodeType = readJs[String](x("nodeType")),
    relation = readJs[Option[String]](x("relation")),
    output = readJs[Option[Seq[String]]](x("output")),
    costs = readJs[PlanNode.Costs](x("costs")),
    properties = readJs[Map[String, String]](x("properties")),
    children = x("children") match {
      case a: Js.Arr => a.value.map(n => readPlanNode(n match {
        case o: Js.Obj => o
        case ex => throw new IllegalStateException(ex.toString)
      }))
      case other => throw new IllegalArgumentException(other.toString)
    }
  )
  implicit val planNodeReader = Reader[PlanNode] {
    case x: Js.Obj => readPlanNode(x)
    case other => throw new IllegalArgumentException(s"Invalid Plan Node [${other.getClass.getSimpleName}].")
  }

  private[this] def writePlanNode(node: PlanNode): Js.Value = Js.Obj(
    "id" -> writeJs(node.id),
    "title" -> writeJs(node.title),
    "nodeType" -> writeJs(node.nodeType),
    "relation" -> writeJs(node.relation),
    "output" -> writeJs(node.output),
    "costs" -> writeJs(node.costs),
    "properties" -> writeJs(node.properties),
    "children" -> Js.Arr(node.children.map(writePlanNode): _*)
  )
  implicit val planNodeWriter = Writer[PlanNode](x => writePlanNode(x))

  // Wire messages
  def readRequestMessage(json: Js.Value) = readJs[RequestMessage](json)
  def writeRequestMessage(rm: RequestMessage, debug: Boolean = false) = if (debug) {
    write(rm, indent = 2)
  } else {
    write(rm)
  }

  def readResponseMessage(json: String) = read[ResponseMessage](json)
  def writeResponseMessageJs(rm: ResponseMessage) = {
    writeJs(rm)
  }
  def writeResponseMessage(rm: ResponseMessage, debug: Boolean = false) = if (debug) {
    write(rm, indent = 2)
  } else {
    write(rm)
  }
}
