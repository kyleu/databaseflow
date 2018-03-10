package services.scalaexport.file.thrift

import models.scalaexport.TwirlFile
import models.scalaexport.thrift.ThriftService

object ThriftTwirlServiceFile {
  def export(
    pkg: Seq[String],
    svc: ThriftService,
    typedefs: Map[String, String],
    enums: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = TwirlFile(Seq("views", "admin", "thrift", svc.identifier), "list")
    file.add(s"@(user: models.user.SystemUser, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "explore", "${svc.name}") {""", 1)
    file.add(s"""<h4>${svc.name} Overview</h4>""")
    file.add("}", -1)
    file
  }
}
