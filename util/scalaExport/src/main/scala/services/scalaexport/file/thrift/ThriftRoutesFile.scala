package services.scalaexport.file.thrift

import models.scalaexport.RoutesFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftRoutesFile {
  private[this] def ws(s: String, i: Int = 60) = s + (0 until (i - s.length)).map(_ => ' ').mkString

  def export(service: ThriftService) = {
    val file = RoutesFile("thrift" + service.name)
    val controllerRef = s"controllers.admin.thrift.${service.identifier}.${service.name}Controller"

    file.add(s"# ${service.name} Routes")
    file.add(s"GET  /${ws("")} $controllerRef.list()")
    file.add()
    service.methods.foreach(m => routeForMethod(m, controllerRef, file))
    file
  }

  def routeForMethod(m: ThriftServiceMethod, controllerRef: String, file: RoutesFile) = {
    file.add(s"GET  /${ws(m.name)} $controllerRef.${m.name}")
    file.add(s"POST /${ws(m.name)} $controllerRef.${m.name}Call")
    file.add()
  }
}
