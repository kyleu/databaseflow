package models.ddl

case object CreateAdHocQueriesTable extends CreateTableStatement("adhoc_queries") {
  override val sql = s"""
    create table $tableName (
      id uuid not null primary key,
      title ${eng.varchar}(1024) not null,
      sql ${eng.varchar}(65536) not null,
      created timestamp not null,
      updated timestamp not null
    );
  """
}
