package util.web

import models.query.QueryResult
import models.schema.ColumnType._
import play.twirl.api.Html

object DataOutputFormatter {
  def cellValue(col: QueryResult.Col, v: Option[String]) = v match {
    case Some(x) => col.t match {
      case StringType if x.isEmpty => Html("<em>empty string</em>")
      case StringType if x.length > 200 => Html(s"""<span title="x.trim">${x.trim.substring(0, 200)}...</span>""")
      case StringType => Html(s"""<span>$x</span>""")
      case IntegerType => Html(s"""<span>$x</span>""")
      case ShortType => Html(s"""<span>$x</span>""")
      case TimestampType => Html(s"""<span>$x</span>""")
      case BooleanType => Html(s"""<span>$x</span>""")
      case BigDecimalType => Html(s"""<span>$x</span>""")
      case LongType => Html(s"""<span>$x</span>""")
      case ByteArrayType => if (x.length > 200) {
        Html(s"""<span title="x.trim">${x.trim.substring(0, 200)}...</span>""")
      } else {
        Html(s"""<span>$x</span>""")
      }
      case _ => Html(s"""<span>$x</span>""")
    }
    case None => Html("""<span title="Null">∅</span>""")
  }
}
