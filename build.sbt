// Database Flow build file. See `./project` for definitions.

useGpg := true

pgpSecretRing := file("/Users/kyle/.gnupg/pubring.kbx")

lazy val doc = Documentation.doc

lazy val sharedJs = Shared.sharedJs

lazy val client = Client.client

lazy val charting = Client.charting

lazy val sharedJvm = Shared.sharedJvm

lazy val dblibs = Database.dblibs

lazy val server = Server.server
