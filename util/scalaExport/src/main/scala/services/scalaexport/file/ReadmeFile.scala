package services.scalaexport.file

object ReadmeFile {
  def content(projectName: String) = {
    s"""# $projectName
    |
    |Using the latest technology in the Scala ecosystem, $projectName is a pure Scala reactive web application built on Play 2.6,
    |ScalaJS, Silhouette 5, Akka, Sangria, and postgres-async.
    |
    |## Running the app
    |
    |First, change the database section of application.conf to use your existing database credentials.
    |
    |You'll either need Node.js available as "node" on the path, or change project/Server.scala's EngineType to Rhino.
    |
    |Now, finally,
    |```shell
    |$$ sbt
    |> run
    |$$ open http://127.0.0.1:9000
    |```
    |
    |As the application starts, it will create database tables and seed data.
    |
    |The first account to sign up is created as an Admin, all subsequent users will have a normal user role.
    |
    |
    |## Projects
    |
    |* `server` Main web application.
    |* `sharedJvm` Core Scala logic and rules definitions, for JVM projects.
    |* `sharedJs` Shared classes, compiled to Scala.js JavaScript.
    |* `client` Barebones Scala.js app.
    |""".stripMargin.trim
  }
}
