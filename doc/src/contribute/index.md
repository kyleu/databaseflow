@@@ index

* [SBT Tasks](sbtTasks.md)
* [Packaging](packaging.md)

@@@

# Contribute

Database Flow is a standard open source Scala SBT project, and can be run like any other SBT application.
Many SBT plugins are provided to make development easier and safer.
To get started, run `sbt` in the project's directory. Once the prompt appears, execute "run", and the service will become available on port 4260.

## Projects

Project                                         | Description
------------------------------------------------|----------------------------------------------------------------------------------------------------
[Shared](../api/shared/index.html)              | Common code shared between JVM and JavaScript, mostly base schema definitions and support classes
[Client](../api/client/index.html)              | ScalaJS classes that create the web user interface
[Scala Export](../api/scalaExport/index.html)   | Logic to export your database to a full-featured Scala project
[Ebenezer](../api/ebenezer/index.html)          | Generates Scala classes and translations for Thrift definitions
[Server](../api/server/index.html)              | Main Database Flow server, handling all http requests and CLI arguments

