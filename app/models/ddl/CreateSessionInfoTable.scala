package models.ddl

case object CreateSessionInfoTable extends CreateTableStatement("session_info") {
  override val sql = s"""
    create table $tableName (
      id ${eng.varchar}(1024) not null,
      provider ${eng.varchar}(64) not null,
      key ${eng.text} not null,
      last_used timestamp without time zone not null,
      expiration timestamp without time zone not null,
      fingerprint ${eng.text},
      created timestamp without time zone not null,
      constraint pk_session_info primary key (id)
    ) with (oids = false);

    create index idx_session_info_provider_key on $tableName (provider, key);
  """
}
