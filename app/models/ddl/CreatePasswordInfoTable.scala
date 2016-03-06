package models.ddl

case object CreatePasswordInfoTable extends CreateTableStatement("password_info") {
  override val sql = s"""
    create table $tableName (
       provider ${eng.varchar}(64) not null,
       key ${eng.text} not null,
       hasher ${eng.varchar}(64) not null,
       password ${eng.varchar}(256) not null,
       salt ${eng.varchar}(256),
       created timestamp without time zone not null,
       constraint pk_$tableName primary key (provider, key)
    ) with (oids = false);
  """
}
