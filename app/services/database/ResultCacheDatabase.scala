package services.database

import java.util.UUID

object ResultCacheDatabase extends CoreDatabase {
  override val connectionId = UUID.fromString("11111111-1111-1111-1111-111111111111")
  override val name = "Result cache"
  override val title = s"${utils.Config.projectName} Result Cache"
  override val description = s"Storage used by ${utils.Config.projectName} to cache query results."
  override val configKey = "resultCache"
  override val dbName = "result-cache"
}
