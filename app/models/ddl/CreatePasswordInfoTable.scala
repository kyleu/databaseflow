package models.ddl

case object CreatePasswordInfoTable extends CreateTableStatement("password_info") {
  override val sql = s"""
    create table $tableName
    (
       provider character varying(64) not null,
       key text not null,
       hasher character varying(64) not null,
       password character varying(256) not null,
       salt character varying(256),
       created timestamp without time zone not null,
       constraint pk_password_info primary key (provider, key)
    ) with (oids = false);
  """
}
