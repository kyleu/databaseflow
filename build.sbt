// Database Flow build file. See `./project` for definitions.

scapegoatVersion in ThisBuild := Dependencies.Utils.scapegoatVersion

useGpg := true

pgpSecretRing := file("/Users/kyle/.gnupg/pubring.kbx")

lazy val doc = Documentation.doc

lazy val sharedJs = Shared.sharedJs

lazy val client = Client.client

lazy val charting = Client.charting

lazy val sharedJvm = Shared.sharedJvm

lazy val dblibs = Database.dblibs

lazy val server = Server.server

lazy val benchmarking = Utilities.benchmarking

lazy val metrics = Utilities.metrics

lazy val translation = Utilities.translation
