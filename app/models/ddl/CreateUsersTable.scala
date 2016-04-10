package models.ddl

case object CreateUsersTable extends CreateTableStatement("flowusers") {
  override val sql = s"""
    create table $tableName (
      id uuid primary key,
      username $varchar(256),
      prefs $varchar(4096) NOT NULL,
      profiles $varchar(1024) not null,
      roles $varchar(512) not null,
      created timestamp not null
    );

    create index ${tableName}_profiles_idx on $tableName (profiles);
    create unique index ${tableName}_username_idx on $tableName (username);
  """
}
