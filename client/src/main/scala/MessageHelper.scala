import models.templates.{ QueryPlanTemplate, QueryResultsTemplate }
import models._
import utils.Logging

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)
    case qr: QueryResult => handleQueryResult(qr)
    case pr: PlanResult => handlePlanResult(pr)
    case _ => Logging.info("Received: " + rm.getClass.getSimpleName)
  }

  private[this] def handleQueryResult(qr: QueryResult) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    workspace.append(html.toString)
  }

  private[this] def handlePlanResult(pr: PlanResult) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    workspace.append(html.toString)
  }
}
