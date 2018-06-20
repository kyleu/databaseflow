package services.explore

import java.util.UUID

import models.result.QueryResultRow
import models.schema.Column
import models.schema.ColumnType._
import sangria.execution.deferred.HasId
import util.DateUtils

object ExploreHasIdHelper {
  type HasIds = Seq[(String, Seq[Column], HasId[QueryResultRow, _])]

  def getHasIds(schema: models.schema.Schema): HasIds = schema.tables.flatMap { table =>
    table.primaryKey.flatMap { pk =>
      pk.columns.toList match {
        case Nil => None
        case single :: Nil =>
          val col = table.columns.find(_.name == single).getOrElse(throw new IllegalStateException(s"Missing column [$single] for table [${table.name}]."))
          if (col.notNull) {
            val hasId = col.columnType match {
              case BigDecimalType => HasId[QueryResultRow, BigDecimal](x => BigDecimal(x.getRequiredCell(single)))
              case BooleanType => HasId[QueryResultRow, Boolean](x => {
                val v = x.getRequiredCell(single)
                v == "true" || v == "1" || v == "yes"
              })
              case ByteType => HasId[QueryResultRow, Byte](_.getRequiredCell(single).toByte)
              case ShortType => HasId[QueryResultRow, Short](_.getRequiredCell(single).toShort)
              case IntegerType => HasId[QueryResultRow, Int](_.getRequiredCell(single).toInt)
              case LongType => HasId[QueryResultRow, Long](_.getRequiredCell(single).toLong)
              case FloatType => HasId[QueryResultRow, Float](_.getRequiredCell(single).toFloat)
              case DoubleType => HasId[QueryResultRow, Double](_.getRequiredCell(single).toDouble)
              case ByteArrayType => HasId[QueryResultRow, Array[Byte]](_.getRequiredCell(single).getBytes)
              case DateType => HasId[QueryResultRow, java.sql.Date](x => DateUtils.sqlDateFromString(x.getRequiredCell(single)))
              case TimeType => HasId[QueryResultRow, java.sql.Time](x => DateUtils.sqlTimeFromString(x.getRequiredCell(single)))
              case TimestampType => HasId[QueryResultRow, java.sql.Timestamp](x => DateUtils.sqlDateTimeFromString(x.getRequiredCell(single)))
              case TimestampZonedType => HasId[QueryResultRow, java.sql.Timestamp](x => DateUtils.sqlZonedDateTimeFromString(x.getRequiredCell(single)))
              case UuidType => HasId[QueryResultRow, UUID](x => UUID.fromString(x.getRequiredCell(single)))
              case _ => HasId[QueryResultRow, String](x => x.getRequiredCell(single))
            }
            Some((table.name, Seq(col), hasId))
          } else {
            val hasId = col.columnType match {
              case BigDecimalType => HasId[QueryResultRow, Option[BigDecimal]](x => x.getCell(single).map(BigDecimal.apply))
              case BooleanType => HasId[QueryResultRow, Option[Boolean]](_.getCell(single).map(v => v == "true" || v == "1" || v == "yes"))
              case ByteType => HasId[QueryResultRow, Option[Byte]](_.getCell(single).map(_.toByte))
              case ShortType => HasId[QueryResultRow, Option[Short]](_.getCell(single).map(_.toShort))
              case IntegerType => HasId[QueryResultRow, Option[Int]](_.getCell(single).map(_.toInt))
              case LongType => HasId[QueryResultRow, Option[Long]](_.getCell(single).map(_.toLong))
              case FloatType => HasId[QueryResultRow, Option[Float]](_.getCell(single).map(_.toFloat))
              case DoubleType => HasId[QueryResultRow, Option[Double]](_.getCell(single).map(_.toDouble))
              case ByteArrayType => HasId[QueryResultRow, Option[Array[Byte]]](_.getCell(single).map(_.getBytes))
              case DateType => HasId[QueryResultRow, Option[java.sql.Date]](x => x.getCell(single).map(DateUtils.sqlDateFromString))
              case TimeType => HasId[QueryResultRow, Option[java.sql.Time]](x => x.getCell(single).map(DateUtils.sqlTimeFromString))
              case TimestampType => HasId[QueryResultRow, Option[java.sql.Timestamp]](x => x.getCell(single).map(DateUtils.sqlDateTimeFromString))
              case TimestampZonedType => HasId[QueryResultRow, Option[java.sql.Timestamp]](x => x.getCell(single).map(DateUtils.sqlZonedDateTimeFromString))
              case UuidType => HasId[QueryResultRow, Option[UUID]](x => x.getCell(single).map(UUID.fromString))
              case _ => HasId[QueryResultRow, Option[String]](x => x.getCell(single))
            }
            Some((table.name, Seq(col), hasId))
          }
        case x => None // Multiple columns
      }
    }
  }
}
