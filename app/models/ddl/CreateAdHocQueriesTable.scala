package models.ddl

case object CreateAdHocQueriesTable extends CreateTableStatement("adhoc_queries") {
  override val sql = s"""
    create table $tableName (
      id uuid not null primary key,
      title text not null,
      sql text not null,
      params text[],
      created timestamp not null,
      updated timestamp not null
    ) with (oids=false);
  """
}
