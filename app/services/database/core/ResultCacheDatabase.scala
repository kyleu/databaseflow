package services.database.core

import java.util.UUID

object ResultCacheDatabase extends CoreDatabase {
  override val connectionId = UUID.fromString("11111111-1111-1111-1111-111111111111")
  override val name = "Result Cache"
  override val slug = "result-cache"
  override val title = s"${util.Config.projectName} Result Cache"
  override val description = s"Storage used by ${util.Config.projectName} to cache query results."
  override val configKey = "resultCache"
  override val dbName = "result-cache"
}
