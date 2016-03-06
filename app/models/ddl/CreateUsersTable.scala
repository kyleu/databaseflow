package models.ddl

case object CreateUsersTable extends CreateTableStatement("flow_users") {
  override val sql = s"""
    create table $tableName (
      id uuid primary key,
      username ${eng.varchar}(256),
      prefs ${eng.varchar}(4096) NOT NULL,
      profiles ${eng.varchar}(1024) not null,
      roles ${eng.varchar}(512) not null,
      created timestamp not null
    );

    create index ${tableName}_profiles_idx on $tableName (profiles);
    create unique index ${tableName}_username_idx on $tableName (username);
  """
}
