package models.ddl

case object CreateAuditRecordTable extends CreateTableStatement("audit_records") {
  override val sql = s"""
    create table $tableName (
      id uuid not null primary key,

      audit_type $varchar(32) not null,

      owner uuid,
      connection uuid not null,

      status $varchar(32) not null,
      attributes $varchar(2048) not null,
      properties $varchar(2048) not null,
      elapsed int not null
    );

    create index idx_${tableName}_user on $tableName (owner);
  """
}
