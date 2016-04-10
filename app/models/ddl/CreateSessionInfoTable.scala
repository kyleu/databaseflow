package models.ddl

case object CreateSessionInfoTable extends CreateTableStatement("session_info") {
  override val sql = s"""
    create table $tableName (
      id $varchar(1024) not null primary key,
      provider $varchar(64) not null,
      key $varchar(2048) not null,
      last_used timestamp not null,
      expiration timestamp not null,
      fingerprint $varchar(65536),
      created timestamp not null
    );

    create index idx_${tableName}_provider_key on $tableName (provider, key);
  """
}
