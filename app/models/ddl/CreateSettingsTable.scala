package models.ddl

case object CreateSettingsTable extends CreateTableStatement("setting_values") {
  override val sql = s"""
    create table "$tableName" (
      "k" $varchar(256) primary key,
      "v" $varchar(4096) not null
    );
  """
}
