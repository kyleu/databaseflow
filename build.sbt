addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.15")

lazy val sharedJs = Shared.sharedJs

lazy val client = Client.client

lazy val charting = Client.charting

lazy val codegen = CodeGen.codegen

lazy val sharedJvm = Shared.sharedJvm

lazy val dblibs = Database.dblibs

lazy val server = Server.server

lazy val site = Site.site

lazy val metrics = Utilities.metrics

lazy val iconCreator = Utilities.iconCreator

lazy val licenseModels = Utilities.licenseModels

lazy val licenseGenerator = Utilities.licenseGenerator

lazy val translation = Utilities.translation

lazy val benchmarking = Utilities.benchmarking
