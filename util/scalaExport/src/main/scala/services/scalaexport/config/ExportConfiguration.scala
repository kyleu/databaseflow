package services.scalaexport.config

import models.schema.ColumnType._
import models.schema.{Column, ColumnType, ForeignKey}
import services.scalaexport.ExportHelper

object ExportConfiguration {
  object Model {
    case class Reference(name: String, srcTable: String, srcCol: String, tgt: String, notNull: Boolean)

    case class Field(
        columnName: String,
        propertyName: String,
        title: String,
        description: Option[String],
        t: ColumnType,
        sqlTypeName: String,
        defaultValue: Option[String],
        notNull: Boolean = false,
        inSearch: Boolean = false,
        inView: Boolean = true,
        ignored: Boolean = false
    ) {
      val className = ExportHelper.toClassName(propertyName)

      val graphQlArgType = {
        val argTypeRaw = t match {
          case StringType => "StringType"
          case BigDecimalType => "Type"
          case BooleanType => "Type"
          case ByteType => "Type"
          case ShortType => "Type"
          case IntegerType => "Type"
          case LongType => "Type"
          case FloatType => "Type"
          case DoubleType => "Type"
          case ByteArrayType => "Type"
          case DateType => "Type"
          case TimeType => "Type"
          case TimestampType => "Type"

          case RefType => "Type"
          case XmlType => "Type"
          case UuidType => "CommonSchema.uuidType"

          case ObjectType => "Type"
          case StructType => "Type"
          case ArrayType => "Type"

          case UnknownType => "Type"
        }
        if (notNull) { argTypeRaw } else { "OptionInputType(" + argTypeRaw + ")" }
      }
    }
  }

  case class Model(
      tableName: String,
      pkg: List[String] = Nil,
      propertyName: String,
      className: String,
      title: String,
      description: Option[String],
      plural: String,
      fields: List[Model.Field],
      pkColumns: List[Column],
      foreignKeys: List[ForeignKey],
      references: List[Model.Reference],
      extendsClass: Option[String] = None,
      ignored: Boolean = false,
      provided: Boolean = false
  ) {
    def getField(k: String) = getFieldOpt(k).getOrElse(throw new IllegalStateException(s"No field for model [$className] with name [$k]."))
    def getFieldOpt(k: String) = fields.find(f => f.columnName == k || f.propertyName == k)
  }
}

case class ExportConfiguration(
    key: String,
    projectId: String,
    projectTitle: String,
    models: Seq[ExportConfiguration.Model],
    engine: String = "postgres",
    projectLocation: Option[String] = None
) {
  def getModel(k: String) = getModelOpt(k).getOrElse(throw new IllegalStateException(s"No model available with name [$k]."))
  def getModelOpt(k: String) = models.find(m => m.tableName == k || m.propertyName == k || m.className == k)
}
