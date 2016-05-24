package services.plan

import java.util.UUID

import models.plan.{ PlanError, PlanResult }
import services.plan.OracleParseHelper.PlanEntry

import scala.util.control.NonFatal

object OracleParseService extends PlanParseService("oracle") {
  override def parse(sql: String, queryId: UUID, plan: String, startMs: Long) = try {
    val parsedPlan = plan.split('\n').flatMap {
      case line if line.trim.isEmpty => None
      case line if line.startsWith("--") => None
      case line if line.startsWith("Plan ") => None
      case line =>
        val split = line.split("\\|").tail
        if (split(0).trim == "Id") {
          None
        } else if (split.length == 7) {
          val (cost, cpu) = {
            val ret = split(5).stripSuffix(")").split("\\(").map(x => OracleParseHelper.stringToInt(x.trim))
            ret(0) -> ret(1)
          }

          Some(PlanEntry(
            id = split(0).trim.toInt,
            operation = split(1).trim,
            depth = split(1).length - split(1).replaceAll("^\\s+", "").length,
            name = if (split(2).trim.nonEmpty) { Some(split(2).trim) } else { None },
            rows = split(3).trim,
            bytes = split(4).trim,
            cost = cost,
            cpu = cpu,
            time = split(6).trim
          ))
        } else {
          throw new IllegalStateException(s"Unable to parse explain plan. Row [$line] contains ${split.length} columns.")
        }
    }

    Right(PlanResult(
      queryId = queryId,
      action = "Action",
      sql = sql,
      raw = plan + "\n\n\n" + parsedPlan.mkString("\n"),
      node = OracleParseHelper.nodeFor(parsedPlan),
      occurred = startMs
    ))
  } catch {
    case NonFatal(x) => Left(PlanError(
      queryId = queryId,
      sql = sql,
      code = x.getClass.getSimpleName,
      message = x.getMessage,
      raw = Some(plan),
      occurred = startMs
    ))
  }
}
