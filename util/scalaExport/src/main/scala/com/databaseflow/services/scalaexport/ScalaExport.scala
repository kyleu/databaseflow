package com.databaseflow.services.scalaexport

import better.files._
import com.databaseflow.models.scalaexport.ScalaExportOptions
import com.databaseflow.models.scalaexport.graphql.GraphQLExportConfig
import com.databaseflow.services.scalaexport.graphql.GraphQLQueryParseService
import com.databaseflow.services.scalaexport.thrift.ThriftParseService
import sangria.schema.Schema
import sangria.parser.QueryParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.control.NonFatal
import util.JsonSerializers._

object ScalaExport {
  private[this] def green(str: String) = 27.toChar + "[32m" + str + 27.toChar + "[39m"
  private[this] def red(str: String) = 27.toChar + "[31m" + str + 27.toChar + "[39m"

  private[this] def log(msg: String, x: Option[Throwable] = None) = {
    println(msg)
    x.foreach(_.printStackTrace())
  }

  def main(args: Array[String]): Unit = {
    ScalaExportOptions.parser.parse(args, ScalaExportOptions()) match {
      case Some(config) => try {
        run(config)
      } catch {
        case NonFatal(x) => log(s"[${red("error")}]  " + x.getMessage, Some(x))
      }
      case None => log(s"[${red("error")}] Cannot parse options from [${args.mkString(", ")}].")
    }
  }

  def run(opts: ScalaExportOptions) = opts.cmd match {
    case "help" => ScalaExportOptions.parser.showUsage()
    case "thrift" => exportThrift(opts.input, opts.output, Set("rest", "graphql", "simple"), configLocation = opts.config.getOrElse("core/src/main/thrift"))
    case "graphql" => exportGraphQL(opts.input)
    case cmd => throw new IllegalStateException(s"Unhandled command [$cmd].")
  }

  def exportGraphQL(input: Option[String]) = {
    val in = input.getOrElse("./tmp/graphql-export.json").toFile
    if (!in.isRegularFile) {
      throw new IllegalStateException(s"Cannot read input file [${in.pathAsString}].")
    }
    val cfg = parseJson(in.contentAsString) match {
      case Right(foo) => foo.as[GraphQLExportConfig].right.get
      case Left(x) => throw x
    }
    val schemaFile = cfg.schema.getOrElse(throw new IllegalStateException("No schema!")).toFile
    val schema = Schema.buildFromAst(QueryParser.parse(schemaFile.contentAsString).toOption.getOrElse(throw new IllegalStateException("Cannot load schema.")))
    new GraphQLQueryParseService(cfg, schema).export()
  }

  def exportThrift(input: Option[String], output: Option[String], flags: Set[String], configLocation: String) = {
    val in = input.getOrElse("./tmp/thrift/test.thrift")
    if (!in.toFile.isRegularFile) {
      throw new IllegalStateException(s"Cannot read input file [$in].")
    }
    val out = output.getOrElse("./tmp/tempoutput")
    if (!out.toFile.isDirectory) {
      throw new IllegalStateException(s"Cannot read output directory [$in].")
    }
    ThriftParseService.exportThrift(filename = in, persist = !flags("inplace"), projectLocation = Some(out), flags = flags, configLocation = configLocation)
  }
}
