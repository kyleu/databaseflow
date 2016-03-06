package models.ddl

case object CreateUsersTable extends CreateTableStatement("users") {
  override val sql = s"""
    create table $tableName (
      id uuid primary key,
      username ${eng.varchar}(256),
      prefs ${eng.text} NOT NULL,
      profiles ${eng.varchar}(1024) not null,
      roles ${eng.varchar}(512) not null,
      created timestamp not null
    ) with (oids=false);

    create index ${tableName}_profiles_idx on $tableName using btree (profiles collate pg_catalog."default");
    create unique index ${tableName}_username_idx on $tableName using btree (username collate pg_catalog."default");
  """
}
