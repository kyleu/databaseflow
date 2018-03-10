package services.scalaexport.file.thrift

import models.scalaexport.TwirlFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftTwirlServiceMethodFile {
  def export(
    pkg: Seq[String],
    svc: ThriftService,
    m: ThriftServiceMethod,
    typedefs: Map[String, String],
    pkgMap: Map[String, Seq[String]]
  ) = {
    val file = TwirlFile(Seq("views", "admin", "thrift", svc.identifier), m.name)
    file.add(s"@(user: models.user.SystemUser, args: String, result: Option[io.circe.Json], debug: Boolean = false)(")
    file.add("    implicit request: Request[AnyContent], session: Session, flash: Flash, traceData: util.tracing.TraceData")
    file.add(s""")@traceData.logViewClass(getClass)@views.html.admin.layout.page(user, "projects", "${svc.name}") {""", 1)
    file.add("""<div class="row">""", 1)
    file.add("""<div class="col s12">""", 1)
    file.add("""<div class="collection with-header">""", 1)

    val routesRef = s"controllers.admin.thrift.${svc.identifier}.routes.${svc.name}Controller"
    file.add("""<div class="collection-header">""", 1)
    file.add(s"""<div class="right"><a class="theme-text" href="@$routesRef.list">${svc.name}</a></div>""")
    file.add(s"<h4>${m.name}</h4>")
    file.add("</div>", -1)

    file.add("""<div class="collection-item">""", 1)
    file.add("""<h5>Arguments</h5>""")
    file.add(s"""<form method="post" action="@$routesRef.${m.name}Call">""", 1)
    file.add("""<div class="input-field">""", 1)
    file.add("""<textarea id="arguments" name="arguments" class="materialize-textarea">@args</textarea>""")
    file.add("</div>", -1)
    file.add("""<button class="btn theme" type="submit">Call</button>""")
    file.add("<form>", -1)
    file.add("</div>", -1)

    file.add("@result.map { r =>", 1)
    file.add("""<div class="collection-item">""", 1)
    file.add("""<h5>Result</h5>""")
    file.add("<pre>@r</pre>")
    file.add("</div>", -1)
    file.add("}", -1)

    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("</div>", -1)
    file.add("}", -1)
    file
  }
}
