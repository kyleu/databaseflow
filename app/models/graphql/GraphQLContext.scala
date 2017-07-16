package models.graphql

import models.user.User
import util.ApplicationContext

case class GraphQLContext(app: ApplicationContext, user: User)
