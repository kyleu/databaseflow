package models.ddl

case object CreateSavedQueriesTable extends CreateTableStatement("saved_queries") {
  override val sql = s"""
    create table $tableName (
      id uuid not null primary key,
      owner uuid not null,
      title ${eng.varchar}(1024) not null,
      sql ${eng.varchar}(65536) not null,
      last_ran timestamp,
      created timestamp not null,
      updated timestamp not null
    );
  """
}
