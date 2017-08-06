package services.scalaexport.inject

import services.scalaexport.ExportHelper

object InjectSearchParams {
  def fromString(s: String) = s.split('/').toList match {
    case pkg :: className :: Nil => InjectSearchParams(pkg = pkg.split('.').toList, className = className, pkColumns = Nil)
    case pkg :: className :: pkColumns :: Nil =>
      val pkCols = pkColumns.split(',').map(_.split(':').toList match {
        case name :: t :: Nil => name -> t
        case x => throw new IllegalStateException(s"Unhandled pkCol [$x].")
      })
      InjectSearchParams(pkg = pkg.split('.').toList, className = className, pkColumns = pkCols)
    case _ => throw new IllegalStateException(s"Cannot parse search params from [$s].")
  }
}

case class InjectSearchParams(pkg: List[String], className: String, pkColumns: Seq[(String, String)]) {
  val identifier = ExportHelper.toIdentifier(className)
  val serviceClass = pkg match {
    case Nil => "services." + className + "Service"
    case _ => "services." + pkg.mkString(".") + "." + className + "Service"
  }
  val viewClass = pkg match {
    case Nil => s"views.html.admin.$identifier.searchResult$className"
    case _ => s"views.html.admin.${pkg.mkString(".")}.$identifier.searchResult$className"
  }
  val message = pkColumns match {
    case Nil => s"""s"$className matched [$$q].""""
    case cols => s"""s"$className [${cols.map(x => "${model." + ExportHelper.toIdentifier(x._1) + "}").mkString(", ")}] matched [$$q].""""
  }

  override def toString = s"${pkg.mkString(".")}/$className/${pkColumns.map(x => x._1 + ":" + x._2).mkString(",")}"
}
