package models.ddl

object CreateConnectionsTable extends CreateTableStatement("connections") {
  override val sql = s"""create table "$tableName" (
    "id" uuid not null primary key,
    "name" varchar(512) not null,
    "slug" varchar(512) not null,
    "owner" uuid,
    "read" varchar(128) not null,
    "edit" varchar(128) not null,
    "description" varchar(4096) not null,
    "engine" varchar(128) not null,
    "host" varchar(2048),
    "db_name" varchar(2048),
    "extra" varchar(2048),
    "url_override" text,
    "username" varchar(512) not null,
    "password" varchar(2048) not null
  );

  create unique index "${tableName}_name_idx" on "$tableName" ("name");
  create unique index "${tableName}_slug_idx" on "$tableName" ("slug");
  """
}
