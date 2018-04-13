package util

import enumeratum._

sealed abstract class Technology(val title: String, val url: String) extends EnumEntry

object Technology extends Enum[Technology] {
  case object Scala extends Technology("Scala", "http://www.scala-lang.org/")
  case object ScalaJs extends Technology("Scala.js", "https://www.scala-js.org/")
  case object PlayFramework extends Technology("Play Framework", "https://www.playframework.com/")
  case object Akka extends Technology("Akka", "http://akka.io/")
  case object Sangria extends Technology("Sangria", "http://sangria-graphql.org/")
  case object GraphiQL extends Technology("GraphiQL", "https://github.com/graphql/graphiql")
  case object GraphQLVoyager extends Technology("GraphQL Voyager", "https://apis.guru/graphql-voyager")
  case object Materialize extends Technology("Materialize CSS", "http://materializecss.com/")
  case object PlotlyJS extends Technology("plotly.js", "https://plot.ly/javascript/")
  case object Enumeratum extends Technology("Enumeratum", "https://github.com/lloydmeta/enumeratum")
  case object Circe extends Technology("circe", "https://circe.github.io/circe")
  case object Scalatags extends Technology("Scalatags", "https://github.com/lihaoyi/scalatags")
  case object HikariCP extends Technology("HikariCP", "https://github.com/brettwooldridge/HikariCP")
  case object Silhouette extends Technology("Silhouette", "http://silhouette.mohiva.com/")
  case object Launch4J extends Technology("Launch4J", "http://launch4j.sourceforge.net/")
  case object Metrics extends Technology("Dropwizard Metrics", "http://metrics.dropwizard.io/")
  case object Netty extends Technology("Netty", "http://netty.io/")
  case object ScalaCrypt extends Technology("Scalacrypt", "https://github.com/Richard-W/scalacrypt")
  //case object ApachePOI extends Technology("Apache POI", "https://poi.apache.org/")
  //case object BouncyCastle extends Technology("BouncyCastle", "https://www.bouncycastle.org/")
  case object FontAwesome extends Technology("Font Awesome", "http://fontawesome.io/")
  case object JQuery extends Technology("JQuery", "https://jquery.com/")
  case object MomentJS extends Technology("Moment.js", "http://momentjs.com/")

  override val values = findValues
}
