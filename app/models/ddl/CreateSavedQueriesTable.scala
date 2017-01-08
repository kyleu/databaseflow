package models.ddl

case object CreateSavedQueriesTable extends CreateTableStatement("saved_queries") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid not null primary key,

      "name" varchar(1024) not null,
      "description" varchar(4096),
      "sql" varchar(65536) not null,
      "params" varchar(4096),

      "owner" uuid,
      "connection" uuid,
      "read" varchar(64) not null,
      "edit" varchar(64) not null,
      "last_ran" timestamp,
      "created" timestamp not null,
      "updated" timestamp not null
    );

    create index "idx_${tableName}_owner" on "$tableName" ("owner");
  """
}
