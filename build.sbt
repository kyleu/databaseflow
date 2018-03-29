// Database Flow build file. See `./project` for definitions.

scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

useGpg := true

pgpSecretRing := file("/Users/kyle/.gnupg/pubring.kbx")

lazy val doc = Documentation.doc

lazy val sharedJs = Shared.sharedJs

lazy val client = Client.client

lazy val charting = Client.charting

lazy val codegen = CodeGen.codegen

lazy val sharedJvm = Shared.sharedJvm

lazy val dblibs = Database.dblibs

lazy val server = Server.server

lazy val site = Site.site

lazy val benchmarking = Utilities.benchmarking

lazy val ebenezer = Utilities.ebenezer

lazy val iconCreator = Utilities.iconCreator

lazy val metrics = Utilities.metrics

lazy val scalaExport = Utilities.scalaExport

lazy val translation = Utilities.translation
