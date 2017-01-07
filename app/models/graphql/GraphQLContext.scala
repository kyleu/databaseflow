package models.graphql

import models.user.User
import utils.ApplicationContext

case class GraphQLContext(app: ApplicationContext, user: User)
