package models.queries

import java.sql.Types._

import models.schema.ColumnType._
import util.Logging

object QueryTranslations extends Logging {
  def forType(i: Int, n: String, colSize: Option[Int] = None, enums: Seq[models.schema.EnumType]) = i match {
    case CHAR | VARCHAR | LONGVARCHAR | CLOB | NCHAR | NVARCHAR | LONGNVARCHAR | NCLOB => enums.find(_.key == n) match {
      case Some(_) => EnumType
      case _ => StringType
    }
    case NUMERIC | DECIMAL => BigDecimalType
    case BIT | BOOLEAN => BooleanType
    case TINYINT => ByteType
    case SMALLINT => ShortType
    case INTEGER | DISTINCT | ROWID => colSize match {
      case Some(x) if x >= 10 => LongType
      case _ => IntegerType
    }
    case BIGINT => LongType
    case REAL | FLOAT => FloatType
    case DOUBLE => DoubleType
    case BINARY | VARBINARY | LONGVARBINARY | BLOB => ByteArrayType
    case DATE => DateType
    case TIME | TIME_WITH_TIMEZONE => TimeType
    case TIMESTAMP if n == "timestamptz" => TimestampZonedType
    case TIMESTAMP => TimestampType
    case TIMESTAMP_WITH_TIMEZONE => TimestampZonedType
    case REF | REF_CURSOR => RefType
    case SQLXML => XmlType
    case OTHER => n match {
      case "uuid" => UuidType
      case "json" => JsonType
      case "jsonb" => JsonType
      case "hstore" => TagsType
      case "ghstore" => TagsType
      case "internal" => StringType
      case "cstring" => StringType
      case "anyelement" => StringType
      case "record" => StringType
      case "citext" => StringType
      case "inet" => StringType
      case "macaddr" => StringType
      case "trigger" => StringType
      case "interval" => StringType
      case "cidr" => StringType
      case "varbit" => StringType
      case x if x.startsWith("gbtreekey") => StringType
      case x =>
        log.warn(s"Encountered unknown field type [$x]. ")
        UnknownType
    }

    case NULL => UnknownType
    case JAVA_OBJECT => ObjectType
    case STRUCT => StructType
    case ARRAY => ArrayType

    case _ =>
      log.warn(s"Encountered unknown column type [$i].")
      UnknownType
  }
}
