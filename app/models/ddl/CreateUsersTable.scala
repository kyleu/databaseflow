package models.ddl

case object CreateUsersTable extends CreateTableStatement("dbf_users") {
  override val sql = s"""
    create table "$tableName" (
      "id" uuid primary key,
      "username" varchar(256),
      "prefs" varchar(4096) not null,
      "email" varchar(1024) not null,
      "role" varchar(64) not null,
      "created" timestamp not null
    );

    create unique index "${tableName}_email_idx" on "$tableName" ("email");
    create unique index "${tableName}_username_idx" on "$tableName" ("username");
  """
}
