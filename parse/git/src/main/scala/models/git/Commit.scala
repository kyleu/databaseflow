package models.git

import org.joda.time.LocalDateTime
import org.eclipse.jgit.revwalk.RevCommit

object Commit {
  def fromJava(rc: RevCommit) = Commit(
    id = rc.getId.getName,
    authorName = rc.getAuthorIdent.getName,
    authorEmail = rc.getAuthorIdent.getEmailAddress,
    committerName = rc.getCommitterIdent.getName,
    committerEmail = rc.getCommitterIdent.getEmailAddress,
    message = rc.getFullMessage.trim,
    occurred = new LocalDateTime(rc.getCommitTime * 1000)
  )
}

case class Commit(
  id: String,
  authorName: String,
  authorEmail: String,
  committerName: String,
  committerEmail: String,
  message: String,
  occurred: LocalDateTime
)
