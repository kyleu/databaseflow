package services.explore

import models.graphql.GraphQLContext
import models.result.QueryResultRow
import sangria.execution.deferred.{Fetcher, HasId}

import scala.concurrent.Future

object ExploreFetcherHelper {
  def getFetchers(schema: models.schema.Schema, hasIds: ExploreHasIdHelper.HasIds) = {
    hasIds.map { hasId =>
      hasId._2.toList match {
        case Nil => throw new IllegalStateException(s"Empty columns for [$hasId].")
        case single :: Nil => Fetcher[GraphQLContext, QueryResultRow, Int]((_, ids) => {
          Future.successful(Seq.empty[QueryResultRow])
        })(hasId._3.asInstanceOf[HasId[QueryResultRow, Int]])
        case _ => throw new IllegalStateException(s"Multiple columns for [$hasId].")
      }
    }
  }
}
