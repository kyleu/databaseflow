package services.scalaexport.file.thrift

import models.scalaexport.RoutesFile
import models.scalaexport.thrift.{ThriftService, ThriftServiceMethod}

object ThriftRoutesFile {
  private[this] def ws(s: String, i: Int = 60) = s + (0 until (i - s.length)).map(_ => ' ').mkString

  def export(service: ThriftService) = {
    val file = RoutesFile("thrift" + service.name)
    val controllerRef = s"controllers.admin.thrift.${service.identifier}.${service.name}Controller"

    file.add(s"# ${service.name} Routes")
    file.add(s"GET ${ws("/")} $controllerRef.list()")
    file.add()
    service.methods.map(m => routeForMethod(m, file)))
    file
  }

  def routeForMethod(m: ThriftServiceMethod, file: RoutesFile) = {
    file.add(s"# ${m.name}")
    file.add(s"# GET  /${m.name}")
    file.add(s"# POST /${m.name}")
    file.add()
  }
}
