package models.queries

import java.sql.Types._

import models.schema.ColumnType._

object QueryTranslations {
  def forType(i: Int) = i match {
    case CHAR | VARCHAR | LONGVARCHAR => StringType
    case NUMERIC | DECIMAL => BigDecimalType
    case BIT => BooleanType
    case TINYINT => ByteType
    case SMALLINT => ShortType
    case INTEGER => IntegerType
    case BIGINT => LongType
    case REAL | FLOAT => FloatType
    case DOUBLE => DoubleType
    case BINARY | VARBINARY | LONGVARBINARY => ByteArrayType
    case DATE => DateType
    case TIME => TimeType
    case TIMESTAMP => TimestampType
  }
}
