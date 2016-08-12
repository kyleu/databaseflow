package models.git

import scala.collection.JavaConverters._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

case class Repo(dir: java.io.File) {
  if (!dir.isDirectory) { throw new IllegalArgumentException("File [] is not a directory.") }
  if (!dir.canRead) { throw new IllegalArgumentException("Directory [] is not readable.") }

  private[this] val repository = new FileRepositoryBuilder().setGitDir(dir).readEnvironment().build()

  val getBranch = Option(repository.getBranch)

  val git = new Git(repository)

  def getCommits() = {
    git.log().call().iterator.asScala.map(Commit.fromJava)
  }
}
