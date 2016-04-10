package models.ddl

case object CreateUserProfilesTable extends CreateTableStatement("user_profiles") {
  override val sql = s"""
    create table $tableName (
      provider $varchar(64) not null,
      key $varchar(1024) not null,
      email $varchar(256),
      first_name $varchar(512),
      last_name $varchar(512),
      full_name $varchar(512),
      avatar_url $varchar(512),
      created timestamp not null,
      constraint ${tableName}_pkey primary key (provider, key)
    );

    create index user_profiles_email_idx on $tableName (email);
  """
}
