package models.ddl

case object CreateSharedResultTable extends CreateTableStatement("shared_results") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid primary key,
      "title" $varchar(512) not null,
      "owner" uuid not null,
      "viewable_by" $varchar(128) not null,
      "connection_id" uuid not null,
      "source_type" $varchar(256),
      "source_name" $varchar(256),
      "source_sort_column" $varchar(128),
      "source_sort_asc" boolean,
      "filter_column" $varchar(128),
      "filter_op" $varchar(4),
      "filter_value" $varchar(256),
      "last_accessed" timestamp not null,
      "created" timestamp not null
    );

    create index "${tableName}_owner_idx" on "$tableName" ("owner");
  """
}
