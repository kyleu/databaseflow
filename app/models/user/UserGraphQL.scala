package models.user

import models.graphql.{CommonGraphQL, GraphQLContext}
import sangria.macros.derive._
import sangria.schema._
import models.graphql.CommonGraphQL._
import models.graphql.DateTimeSchema._

object UserGraphQL {
  implicit val roleEnum = CommonGraphQL.deriveEnumeratumType(
    name = "Role",
    description = "The role of the system user.",
    values = Role.values.map(t => t -> t.entryName).toList
  )

  implicit val profileType = deriveObjectType[GraphQLContext, UserProfile](ObjectTypeDescription("Information about the current session."))

  val queryFields = fields[GraphQLContext, Unit](
    Field(
      name = "profile",
      description = Some("Returns information about the currently logged in user."),
      fieldType = profileType,
      resolve = c => UserProfile(
        c.ctx.user.id, c.ctx.user.username, c.ctx.user.profile.providerKey, c.ctx.user.role.toString, c.ctx.user.created
      )
    )
  )
}
