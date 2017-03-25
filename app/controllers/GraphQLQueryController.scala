package controllers

import java.util.UUID

import models.forms.GraphQLForm
import models.graphql.GraphQLQuery
import services.connection.ConnectionSettingsService
import services.graphql.GraphQLQueryService
import utils.ApplicationContext

import scala.concurrent.Future
import scala.util.control.NonFatal

@javax.inject.Singleton
class GraphQLQueryController @javax.inject.Inject() (override val ctx: ApplicationContext) extends BaseController {
  def load(connection: String, queryId: UUID) = withSession("graphql.load") { implicit request =>
    val connOpt = try {
      val connUuid = UUID.fromString(connection)
      ConnectionSettingsService.getById(connUuid)
    } catch {
      case NonFatal(_) => ConnectionSettingsService.getBySlug(connection)
    }

    Future.successful(connOpt match {
      case Some(c) =>
        val q = GraphQLQueryService.getById(queryId, Some(request.identity))
        Redirect(controllers.routes.GraphQLController.graphql(c.slug, Some(queryId)).url)
      case None => Redirect(routes.HomeController.home())
    })
  }

  def save(conn: UUID) = withSession("graphql.save") { implicit request =>
    val result = GraphQLForm.form.bindFromRequest.fold(
      formWithErrors => BadRequest(formWithErrors.value.map(_.name).getOrElse(messagesApi("Unknown error"))),
      gqlf => {
        val gqlOpt = gqlf.id.map(id => GraphQLQueryService.getById(id, Some(request.identity)).getOrElse(throw new IllegalStateException("Not allowed.")))
        val gql = gqlOpt.getOrElse(GraphQLQuery.empty(request.identity.id))
        val updated = gql.copy(
          connection = gqlf.connection,
          category = gqlf.category,
          name = gqlf.name,
          query = gqlf.query,
          read = gqlf.read,
          edit = gqlf.edit
        )
        gqlOpt match {
          case Some(existing) =>
            GraphQLQueryService.update(updated, request.identity)
            Ok("Updated")
          case None =>
            GraphQLQueryService.insert(updated)
            Ok("Inserted")
        }
      }
    )
    Future.successful(result)
  }
}
