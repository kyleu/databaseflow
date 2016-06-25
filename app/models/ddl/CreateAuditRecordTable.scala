package models.ddl

case object CreateAuditRecordTable extends CreateTableStatement("audit_records") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid not null primary key,

      "audit_type" $varchar(32) not null,

      "owner" uuid,
      "connection" uuid,

      "status" $varchar(32) not null,
      "sql" text,
      "error" text,
      "rows_affected" int,
      "elapsed" int not null,
      "occurred" timestamp not null
    );

    create index "idx_${tableName}_user" on "$tableName" ("owner");
  """
}
