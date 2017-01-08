package models.ddl

case object CreatePasswordInfoTable extends CreateTableStatement("password_info") {
  override val sql = s"""
    create table "$tableName" (
       "provider" varchar(64) not null,
       "key" varchar(2048) not null,
       "hasher" varchar(64) not null,
       "password" varchar(256) not null,
       "salt" varchar(256),
       "created" timestamp not null,
       constraint "${tableName}_pkey" primary key ("provider", "key")
    );
  """
}
