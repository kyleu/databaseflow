package controllers

import java.util.UUID

import akka.util.Timeout
import models.database.ConnectionSettings
import models.queries.{AdHocQuery, AdHocQueries}
import org.joda.time.LocalDateTime
import play.api.data.Form
import play.api.data.Forms._
import services.database.DatabaseService
import utils.{DateUtils, ApplicationContext}

import scala.concurrent.Future
import scala.concurrent.duration._

@javax.inject.Singleton
class AdHocQueryController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  case class QueryExecution(action: String, id: Option[String], title: String, sql: String)

  val executionForm = Form(
    mapping(
      "action" -> nonEmptyText,
      "id" -> optional(text),
      "title" -> text,
      "sql" -> nonEmptyText
    )(QueryExecution.apply)(QueryExecution.unapply)
  )

  implicit val timeout = Timeout(10.seconds)

  val cs = ConnectionSettings(
    name = Some("adhoc-temp"),
    url = "jdbc:postgresql://localhost:5432/puzzlebrawl",
    username = "databaseflow",
    password = "flow"
  )
  val db = DatabaseService.connect(cs)

  def queryList(query: Option[UUID], action: Option[String]) = act("list") { implicit request =>
    val ret = if (action.contains("load")) {
      val queries = db.query(AdHocQueries.search("", "title", None))
      val q = query.flatMap(x => queries.find(_.id == x))
      Ok(views.html.adhoc.adhoc(query, q.map(_.sql).getOrElse(""), Seq.empty -> Seq.empty, 0, queries))
    } else if (action.contains("delete")) {
      val ok = db.execute(AdHocQueries.removeById(Seq(query.getOrElse(throw new IllegalStateException()))))
      Redirect(controllers.routes.AdHocQueryController.queryList(query, Some("load")))
    } else {
      val queries = db.query(AdHocQueries.search("", "title", None))
      Ok(views.html.adhoc.adhoc(query, "", Seq.empty -> Seq.empty, 0, queries))
    }
    Future.successful(ret)
  }

  def run() = act("run") { implicit request =>
    import DateUtils._

    val ret = executionForm.bindFromRequest.fold(
      formWithErrors => BadRequest("Couldn't process form:\n  " + formWithErrors.errors.mkString("  \n")),
      form => form.action match {
        case "save" => if (form.id.isEmpty || form.id.getOrElse("").isEmpty) {
          val q = AdHocQuery(UUID.randomUUID, form.title, form.sql, new LocalDateTime, new LocalDateTime)
          val ok = db.execute(AdHocQueries.insert(q))
          val queries = db.query(AdHocQueries.search("", "title", None))
          val newId = queries.sortBy(_.created).headOption.map(_.id)
          Ok(views.html.adhoc.adhoc(newId, form.sql, Seq.empty -> Seq.empty, 0, queries))
        } else {
          val queryId = form.id.map(UUID.fromString)
          val q = AdHocQueries.UpdateAdHocQuery(queryId.getOrElse(throw new IllegalStateException()), form.title, form.sql)
          val ok = db.execute(q)
          val queries = db.query(AdHocQueries.search("", "title", None))
          Ok(views.html.adhoc.adhoc(queryId, form.sql, Seq.empty -> Seq.empty, 0, queries))
        }
        case "run" =>
          val queries = db.query(AdHocQueries.search("", "title", None))
          val startTime = System.nanoTime
          val result = db.query(AdHocQueries.AdHocQueryExecute(form.sql, Seq.empty))
          val executionTime = ((System.nanoTime - startTime) / 1000000).toInt
          val queryId = form.id.map(UUID.fromString)
          Ok(views.html.adhoc.adhoc(queryId, form.sql, result, executionTime, queries))
        case x => throw new IllegalStateException(x)
      }
    )
    Future.successful(ret)
  }
}
