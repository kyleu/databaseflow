package models.ddl

case object CreateUserProfilesTable extends CreateTableStatement("user_profiles") {
  override val sql = s"""
    create table $tableName (
      provider ${eng.varchar}(64) not null,
      key ${eng.varchar}(1024) not null,
      email ${eng.varchar}(256),
      first_name ${eng.varchar}(512),
      last_name ${eng.varchar}(512),
      full_name ${eng.varchar}(512),
      avatar_url ${eng.varchar}(512),
      created timestamp not null
    ) with (oids=false);

    create index user_profiles_email_idx on $tableName using btree (email collate pg_catalog."default");

    alter table $tableName add constraint ${tableName}_provider_key_idx unique (provider, key);
  """
}
