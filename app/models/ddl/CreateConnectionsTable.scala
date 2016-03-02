package models.ddl

object CreateConnectionsTable extends CreateTableStatement("connections") {
  override def sql: String = s"""
    create table $tableName (
      id uuid not null primary key,
      name character varying(256) not null,
      engine character varying(128) not null,
      url character varying(2048) not null
    );
  """
}
