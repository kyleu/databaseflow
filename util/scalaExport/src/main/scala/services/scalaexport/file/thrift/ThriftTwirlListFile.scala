package services.scalaexport.file.thrift

import models.scalaexport.TwirlFile
import models.scalaexport.thrift.ThriftService

object ThriftTwirlListFile {
  def export(service: ThriftService) = {
    val file = TwirlFile(Seq("views", "admin", "thrift", service.identifier), "list")
    file.add(s"@(user: models.user.SystemUser, debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "explore", "${service.name}") {""", 1)
    file.add(s"<ul>", 1)
    service.methods.foreach { m =>
      file.add(s"<li>${m.name}</li>")
    }
    file.add(s"</ul>", -1)
    file.add("}", -1)
    file
  }
}
