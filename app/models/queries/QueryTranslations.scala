package models.queries

import java.sql.Types._

import models.schema.ColumnType._
import util.Logging

object QueryTranslations extends Logging {
  def forType(i: Int, n: String, colSize: Option[Int] = None) = i match {
    case CHAR | VARCHAR | LONGVARCHAR | CLOB | NCHAR | NVARCHAR | LONGNVARCHAR | NCLOB => StringType
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
    case TIMESTAMP | TIMESTAMP_WITH_TIMEZONE => TimestampType
    case REF | REF_CURSOR => RefType
    case SQLXML => XmlType
    case OTHER => n match {
      case "uuid" => UuidType
      case "json" => StringType
      case "jsonb" => StringType
      case "hstore" => TagsType
      case "ghstore" => TagsType
      case "internal" => StringType
      case "cstring" => StringType
      case "anyelement" => StringType
      case "record" => StringType
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
