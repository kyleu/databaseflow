package services.scalaexport.file.thrift

import models.scalaexport.TwirlFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftTwirlServiceFile {
  def export(
    pkg: Seq[String],
    svc: ThriftService,
    typedefs: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = TwirlFile(Seq("views", "admin", "thrift", svc.identifier), "list")
    file.add(s"@(user: models.user.SystemUser, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "projects", "${svc.name}") {""", 1)
    file.add("""<div class="row">""", 1)
    file.add("""<div class="col s12">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("""<div class="collection-header">""", 1)
    file.add(s"<h4>${svc.name}</h4>")
    file.add(s"<em>${pkg.mkString(".")}</h4>")
    file.add("</div>", -1)

    svc.methods.foreach(m => methodLink(file, m, typedefs, pkgMap))

    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("}", -1)
    file
  }

  private[this] def methodLink(file: TwirlFile, m: ThriftServiceMethod, typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    val retVal = ThriftFileHelper.columnTypeFor(m.returnValue, typedefs = typedefs, pkgMap)._1
    val argVals = m.arguments.map(arg => arg.name + ": " + ThriftFileHelper.columnTypeFor(arg.t, typedefs = typedefs, pkgMap)._1)
    val sig = s"${m.name}(${argVals.mkString(", ")}): $retVal"
    file.add(s"""<a class="theme-text collection-item" href="...">$sig</a>""")
  }
}
