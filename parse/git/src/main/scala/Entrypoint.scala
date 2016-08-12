import models.git.Repo

object Entrypoint {
  def main(args: Array[String]) = {
    val repo = Repo(new java.io.File("./.git"))
    println(repo.getBranch)
    repo.getCommits().foreach(println)
  }
}
