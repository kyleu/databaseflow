package models.audit

import enumeratum._
import models.template.Icons

object AuditType extends Enum[AuditType] {
  case object SignIn extends AuditType("Sign In", Icons.signIn)
  case object SignOut extends AuditType("Sign Out", Icons.signOut)

  case object CreateConnection extends AuditType("Create Connection", Icons.sliders)
  case object EditConnection extends AuditType("Edit Connection", Icons.edit)
  case object DeleteConnection extends AuditType("Delete Connection", Icons.recycle)

  case object Connect extends AuditType("Connect", Icons.star)
  case object Disconnect extends AuditType("Disconnect", Icons.starOpen)

  case object SaveQuery extends AuditType("Save Query", Icons.savedQuery)
  case object DeleteQuery extends AuditType("Delete Query", Icons.close)
  case object Query extends AuditType("Query", Icons.adHocQuery)
  case object Execute extends AuditType("Execute", Icons.statementResults)

  case object SaveGraphQL extends AuditType("Save GraphQL", Icons.graphQL)
  case object DeleteGraphQL extends AuditType("Delete GraphQL", Icons.close)

  override val values = findValues
}

sealed abstract class AuditType(val title: String, val icon: String) extends EnumEntry
