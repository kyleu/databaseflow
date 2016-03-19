import models.templates.{ QueryErrorTemplate, QueryPlanTemplate, QueryResultsTemplate }
import models._
import utils.Logging
import org.scalajs.jquery.{ jQuery => $, JQueryEventObject }

trait MessageHelper { this: DatabaseFlow =>
  protected[this] def handleMessage(rm: ResponseMessage) = rm match {
    case p: Pong => latencyMs = Some((System.currentTimeMillis - p.timestamp).toInt)
    case is: InitialState => onInitialState(is)
    case qr: QueryResult => handleQueryResult(qr)
    case qe: QueryError => handleQueryError(qe)
    case pr: PlanResult => handlePlanResult(pr)
    case _ => Logging.info("Received: " + rm.getClass.getSimpleName)
  }

  private[this] def handleQueryResult(qr: QueryResult) = {
    //Logging.info(s"Received result with [${qr.columns.size}] columns and [${qr.data.size}] rows.")
    val html = QueryResultsTemplate.forResults(qr)
    workspace.append(html.toString)
    $(s"#${qr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qr.id}").remove()
    })
  }

  private[this] def handleQueryError(qe: QueryError) = {
    val html = QueryErrorTemplate.forError(qe)
    workspace.append(html.toString)
    $(s"#${qe.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${qe.id}").remove()
    })
  }

  private[this] def handlePlanResult(pr: PlanResult) = {
    //Logging.info(s"Received plan with [${pr.plan.maxRows}] rows.")
    val html = QueryPlanTemplate.forPlan(pr)
    workspace.append(html.toString)
    $(s"#${pr.id} .fa-close").click((e: JQueryEventObject) => {
      $(s"#${pr.id}").remove()
    })
  }
}
