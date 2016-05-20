package models.ddl

case object CreateSavedQueriesTable extends CreateTableStatement("saved_queries") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid not null primary key,

      "name" $varchar(1024) not null,
      "description" $varchar(4096),
      "sql" $varchar(65536) not null,

      "owner" uuid,
      "connection" uuid,
      "public" boolean not null default false,
      "last_ran" timestamp,
      "created" timestamp not null,
      "updated" timestamp not null
    );

    create index "idx_${tableName}_owner" on "$tableName" ("owner");
  """
}
