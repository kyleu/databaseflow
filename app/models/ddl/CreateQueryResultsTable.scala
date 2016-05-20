package models.ddl

case object CreateQueryResultsTable extends CreateTableStatement("query_results") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid primary key,
      "query_id" uuid not null,
      "connection_id" uuid not null,
      "owner" uuid,
      "status" $varchar(32),
      "sql" text,
      "columns" int not null default 0,
      "rows" int not null default 0,
      "duration" int not null default 0,
      "last_accessed" timestamp,
      "created" timestamp not null
    );

    create unique index "${tableName}_owner_idx" on "$tableName" ("owner");
  """
}
