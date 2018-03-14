package services.scalaexport.db.file

import models.scalaexport.db.ExportField
import models.scalaexport.file.ThriftFile

object ThriftServiceMutations {
  private[this] val credsParam = "1: common.Credentials creds,"

  def writeMutations(file: ThriftFile, pkFields: List[ExportField], retType: String) = {
    // TODO
    file.add(s"$retType insert(", 1)
    file.add(credsParam)
    file.add(s"2: required $retType model")
    file.add(")", -1)
    file.add("common.int insertBatch(", 1)
    file.add(credsParam)
    file.add(s"2: required list<$retType> models")
    file.add(")", -1)

    pkFields match {
      case Nil => // noop
      case pkf :: Nil =>
        file.add(s"$retType create(", 1)
        file.add(credsParam)
        file.add(s"2: ${pkf.thriftVisibility}  ${pkf.thriftType} ${pkf.propertyName},")
        file.add("3: list<common.DataField> fields")
        file.add(")", -1)

        file.add(s"$retType remove(", 1)
        file.add(credsParam)
        file.add(s"2: ${pkf.thriftVisibility}  ${pkf.thriftType} ${pkf.propertyName}")
        file.add(")", -1)

        file.add(s"$retType update(", 1)
        file.add(credsParam)
        file.add(s"2: ${pkf.thriftVisibility}  ${pkf.thriftType} ${pkf.propertyName},")
        file.add("3: list<common.DataField> fields")
        file.add(")", -1)
      case multi => // noop
    }
  }
}
