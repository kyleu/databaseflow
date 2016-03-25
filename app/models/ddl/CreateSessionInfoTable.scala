package models.ddl

case object CreateSessionInfoTable extends CreateTableStatement("session_info") {
  override val sql = s"""
    create table $tableName (
      id ${eng.varchar}(1024) not null primary key,
      provider ${eng.varchar}(64) not null,
      key ${eng.varchar}(2048) not null,
      last_used timestamp not null,
      expiration timestamp not null,
      fingerprint ${eng.varchar}(65536),
      created timestamp not null
    );

    create index idx_session_info_provider_key on $tableName (provider, key);
  """
}
