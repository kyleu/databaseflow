package services.codegen

import java.sql.JDBCType

import models.codegen.{ JdbcTypes, Capabilities, Engine }
import org.hibernate.dialect.Dialect
import org.hibernate.dialect.function._

import scala.util.control.NonFatal

object CapabilitiesProvider {
  def capabilitiesFor(engine: Engine) = {
    val dialect = DialectProvider.dialectFor(engine)

    val functions = getFunctions(dialect)

    dialect.getQuerySequencesString

    val types = JDBCType.values.sortBy(_.toString).map { x =>
      try {
        x.toString -> Option(dialect.getTypeName(x.getVendorTypeNumber))
      } catch {
        case NonFatal(ex) => x.toString -> None
      }
    }

    Capabilities(
      engine = engine,
      columnTypes = types,
      builtInFunctions = functions
    )
  }

  private[this] def getFunctions(dialect: Dialect) = {
    import collection.JavaConverters._
    dialect.getFunctions.asScala.toSeq.sortBy(_._1).map { f =>
      val name = f._1
      val typ = f._2 match {
        case x: StandardSQLFunction => "standard"
        case x: VarArgsSQLFunction => "varargs"
        case x: NoArgSQLFunction => "noargs"
        case x: SQLFunctionTemplate => "template"
        case x: PositionSubstringFunction => "substring"
        case x: CastFunction => "cast"
        case unknown => throw new IllegalArgumentException(s"Unhandled function type [${unknown.getClass.getName}].")
      }
      Capabilities.SqlFunction(name, typ)
    }
  }
}
