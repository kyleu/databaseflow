package models.user

import java.util.UUID

import org.joda.time.LocalDateTime

case class UserProfile(
  id: UUID,
  username: String,
  email: String,
  role: String,
  created: LocalDateTime
)
