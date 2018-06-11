package models.scalaexport.db

import models.scalaexport.db.config.ExportConfiguration
import models.schema.{Column, ColumnType, ForeignKey}
import services.scalaexport.ExportHelper
import util.JsonSerializers._

object ExportModel {
  object Reference {
    implicit val jsonEncoder: Encoder[Reference] = deriveEncoder
    implicit val jsonDecoder: Decoder[Reference] = deriveDecoder
  }

  case class Reference(name: String, propertyName: String = "", srcTable: String, srcCol: String, tgt: String, notNull: Boolean)

  implicit val jsonEncoder: Encoder[ExportModel] = deriveEncoder
  implicit val jsonDecoder: Decoder[ExportModel] = deriveDecoder
}

case class ExportModel(
    tableName: String,
    pkg: List[String] = Nil,
    propertyName: String,
    className: String,
    title: String,
    description: Option[String],
    plural: String,
    fields: List[ExportField],
    pkColumns: List[Column],
    foreignKeys: List[ForeignKey],
    references: List[ExportModel.Reference],
    extendsClass: Option[String] = None,
    icon: Option[String] = None,
    scalaJs: Boolean = false,
    ignored: Boolean = false,
    audited: Boolean = false,
    provided: Boolean = false
) {
  val fullClassName = (pkg :+ className).mkString(".")
  val propertyPlural = ExportHelper.toIdentifier(plural)
  val pkFields = pkColumns.map(c => getField(c.name))
  val pkType = pkFields match {
    case Nil => "???"
    case h :: Nil => h.scalaType
    case cols => "(" + cols.map(_.scalaType).mkString(", ") + ")"
  }

  val indexedFields = fields.filter(_.indexed).filterNot(pkFields.contains).filterNot(_.t == ColumnType.TagsType)
  val searchFields = fields.filter(_.inSearch)

  val pkgString = pkg.mkString(".")
  val summaryFields = fields.filter(_.inSummary).filterNot(x => pkFields.exists(_.columnName == x.columnName))

  val viewPackage = "views" +: "admin" +: pkg
  val viewHtmlPackage = "views" +: "html" +: "admin" +: pkg

  val modelPackage = "models" +: pkg
  val modelClass = (modelPackage :+ className).mkString(".")

  val queriesPackage = "models" +: "queries" +: pkg
  val tablePackage = "models" +: "table" +: pkg

  val servicePackage = "services" +: pkg
  val serviceDirectory = "app"
  val serviceClass = (servicePackage :+ (className + "Service")).mkString(".")
  val serviceReference = pkg match {
    case Nil => "services." + propertyName + "Service"
    case _ => "services." + pkg.head + "Services." + propertyName + "Service"
  }

  val controllerPackage = "controllers" +: "admin" +: (if (pkg.isEmpty) { List("system") } else { pkg })
  val controllerDirectory = "app"
  val controllerClass = (controllerPackage :+ (className + "Controller")).mkString(".")
  val truncatedController = tableName == "system_users"

  val routesPackage = controllerPackage :+ "routes"
  val routesClass = (routesPackage :+ (className + "Controller")).mkString(".")
  def iconHtml(rootPrefix: String) = s"""<i class="fa @${rootPrefix}models.template.Icons.$propertyName"></i>"""

  val pkArgs = pkFields.zipWithIndex.map(pkf => pkf._1.t match {
    case ColumnType.EnumType => s"enumArg(${pkf._1.enumOpt.getOrElse(throw new IllegalStateException("Cannot load enum.")).fullClassName})(arg(${pkf._2}))"
    case _ => s"${pkf._1.t.value}Arg(arg(${pkf._2}))"
  }).mkString(", ")

  def validReferences(config: ExportConfiguration) = {
    references.filter(ref => config.getModelOpt(ref.srcTable).isDefined)
  }
  def transformedReferences(config: ExportConfiguration) = validReferences(config).flatMap { r =>
    val src = config.getModel(r.srcTable)
    getFieldOpt(r.tgt).flatMap(f => src.getFieldOpt(r.srcCol).map(tf => (r, f, src, tf)))
  }

  def transformedReferencesDistinct(config: ExportConfiguration) = {
    transformedReferences(config).groupBy(x => x._2 -> x._3).toSeq.sortBy(_._1._2.className).map(_._2.head)
  }

  def getField(k: String) = getFieldOpt(k).getOrElse {
    throw new IllegalStateException(s"No field for model [$className] with name [$k]. Available fields: [${fields.map(_.propertyName).mkString(", ")}].")
  }
  def getFieldOpt(k: String) = fields.find(f => f.columnName == k || f.propertyName == k)
}
