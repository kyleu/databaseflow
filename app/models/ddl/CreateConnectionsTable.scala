package models.ddl

object CreateConnectionsTable extends CreateTableStatement("connections") {
  override def sql: String = s"""
    create table "$tableName" (
      "id" uuid not null primary key,
      "name" $varchar(256) not null,
      "owner" uuid,
      "read" $varchar(128) not null,
      "edit" $varchar(128) not null,
      "description" $varchar(4096) not null,
      "engine" $varchar(128) not null,
      "url" $varchar(2048) not null,
      "username" $varchar(512) not null,
      "password" $varchar(2048) not null
    );
  """
}
