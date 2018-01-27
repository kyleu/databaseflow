package services.scalaexport.file

import models.scalaexport.ThriftFile

object ThriftServiceRelations {
  def writeRelations(file: ThriftFile) = {
    // TODO
    /*
      common.int countByUserId(
        1: common.Credentials creds,
        2: common.UUID userId
      )
      list<audit.Audit> getByUserId(
        1: common.Credentials creds,
        2: common.UUID id,
        3: list<result.OrderBy> orderBys,
        4: common.int limit,
        5: common.int offset
      );
      list<audit.Audit> getByUserIdSeq(
        1: common.Credentials creds,
        2: list<common.UUID> idSeq
      );
     */
  }
}
