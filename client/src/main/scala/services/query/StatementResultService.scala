package services.query

import java.util.UUID

import models.query.QueryResult
import models.template.query.StatementResultsTemplate
import org.scalajs.jquery.{jQuery => $}
import ui.ProgressManager
import util.TemplateUtils

object StatementResultService {
  def handleNewStatementResults(resultId: UUID, index: Int, result: QueryResult, durationMs: Int): Unit = {
    val occurred = new scalajs.js.Date(result.occurred.toDouble)
    TransactionService.incrementCount()

    val content = StatementResultsTemplate.forStatementResults(result, occurred.toISOString, durationMs, resultId)
    ProgressManager.completeProgress(result.queryId, resultId, index, content)

    val panel = $(s"#$resultId", $(s"#workspace-${result.queryId}"))
    val sqlEl = $(".statement-result-sql", panel)
    var sqlShown = false
    TemplateUtils.clickHandler($(".results-sql-link", panel), _ => {
      if (sqlShown) { sqlEl.hide() } else { sqlEl.show() }
      sqlShown = !sqlShown
    })
  }
}
