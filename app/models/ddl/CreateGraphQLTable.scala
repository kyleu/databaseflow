package models.ddl

case object CreateGraphQLTable extends CreateTableStatement("graphql") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid primary key,
      "connection_id" uuid,
      "category" varchar(512),
      "name" varchar(512) not null,
      "query" varchar(65536),
      "owner" uuid not null,
      "read" varchar(128) not null,
      "edit" varchar(128) not null,
      "created" timestamp not null
    );

    create index "${tableName}_owner_idx" on "$tableName" ("owner");
  """
}
