package services.codegen

import java.sql.JDBCType

import models.codegen.{Capabilities, Engine}
import org.hibernate.dialect.Dialect
import org.hibernate.dialect.function._

import scala.util.control.NonFatal

object CapabilitiesProvider {
  def capabilitiesFor(engine: Engine) = {
    val dialect = DialectProvider.dialectFor(engine)

    val functions = dialect.functions()

    val types = dialect.types()

    Capabilities(
      engine = engine,
      columnTypes = types,
      functions = functions
    )
  }

  def getFunctions(dialect: Dialect) = {
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
        case x: NvlFunction => "nvl"
        case x: AnsiTrimEmulationFunction => "ansitrim"
        case unknown => throw new IllegalArgumentException(s"Unhandled function type [${unknown.getClass.getName}].")
      }
      Capabilities.SqlFunction(name, typ)
    }
  }

  def getTypes(dialect: Dialect) = {
    JDBCType.values.sortBy(_.toString).map { x =>
      try {
        val name = Option(dialect.getTypeName(x.getVendorTypeNumber)) match {
          case Some(t) if t.indexOf('(') > -1 => Some(t.substring(0, t.indexOf('(')))
          case o => o
        }
        x.toString -> name
      } catch {
        case NonFatal(ex) => x.toString -> None
      }
    }
  }
}
