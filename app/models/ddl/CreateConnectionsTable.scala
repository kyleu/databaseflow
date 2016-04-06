package models.ddl

object CreateConnectionsTable extends CreateTableStatement("connections") {
  override def sql: String = s"""
    create table $tableName (
      id uuid not null primary key,
      name ${eng.varchar}(256) not null,
      owner uuid,
      public boolean not null,
      description ${eng.varchar}(4096) not null,
      engine ${eng.varchar}(128) not null,
      url ${eng.varchar}(2048) not null,
      username ${eng.varchar}(512) not null,
      password ${eng.varchar}(2048) not null
    );
  """
}
