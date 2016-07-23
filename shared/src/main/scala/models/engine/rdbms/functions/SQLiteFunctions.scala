/* Generated Code */
// scalastyle:off
package models.engine.rdbms.functions

import models.engine.DatabaseEngine

trait SQLiteFunctions extends DatabaseEngine {
  override val builtInFunctions = Seq(
    "concat",
    "mod",
    "substr",
    "substring"
  )
}
// scalastyle:on
