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
    val file = TwirlFile(Seq("views", "admin", "thrift"), svc.identifier)
    file.add(s"@(user: models.user.SystemUser, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "projects", "${svc.name}") {""", 1)
    file.add("""<div class="row">""", 1)
    file.add("""<div class="col s12">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    file.add("""<div class="collection-header">""", 1)
    file.add(s"<h4>${svc.name}</h4>")
    file.add(s"<em>${pkg.mkString(".")}</em>")
    file.add("</div>", -1)

    val routesRef = s"controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller"
    svc.methods.foreach(m => methodLink(file, m, routesRef, typedefs, pkgMap))

    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("}", -1)
    file
  }

  private[this] def methodLink(file: TwirlFile, m: ThriftServiceMethod, ref: String, typedefs: Map[String, String], pkgMap: Map[String, Seq[String]]) = {
    file.add(s"""<a class="theme-text collection-item" href="@$ref.${m.name}">${m.sig(typedefs, pkgMap)}</a>""")
  }
}
