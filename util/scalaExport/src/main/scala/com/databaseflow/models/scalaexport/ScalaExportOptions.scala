package com.databaseflow.models.scalaexport

object ScalaExportOptions {
  val parser = new scopt.OptionParser[ScalaExportOptions](programName = "Database Flow Scala Export") {
    head("Database Flow Scala Export", "1.0")
    opt[Unit]('h', "help").action((x, c) => c.copy(cmd = "help")).text("Prints this help message and exits.")
    cmd("database").action((_, c) => c.copy(cmd = "database")).text("Exports a database schema to a Scala project.").children(
      opt[String]("input").abbr("in").action((x, c) => c.copy(input = Some(x))).text("Source schema to read from (name or id)"),
      opt[String]("output").abbr("out").action((x, c) => c.copy(output = Some(x))).text("Directory to export project to"),
      opt[String]("config").abbr("cfg").action((x, c) => c.copy(config = Some(x))).text("Configuration file to read options from")
    )
    cmd("graphql").action((_, c) => c.copy(cmd = "graphql")).text("Exports Scala representations of GraphQL queries. A work-in-progress.")
    cmd("thrift").action((_, c) => c.copy(cmd = "thrift")).text("Exports a thrift definition to Scala classes.").children(
      opt[String]("input").abbr("in").action((x, c) => c.copy(input = Some(x))).text("Thrift file to read"),
      opt[String]("output").abbr("out").action((x, c) => c.copy(output = Some(x))).text("Directory to export files to")
    )
  }
}

case class ScalaExportOptions(
    cmd: String = "help",
    input: Option[String] = None,
    output: Option[String] = None,
    config: Option[String] = None
)
