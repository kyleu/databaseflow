import sbt._
import com.typesafe.sbt.packager.universal.UniversalKeys

object Build extends Build with UniversalKeys {
  lazy val sharedJs = Shared.sharedJs
  lazy val client = Client.client

  lazy val sharedJvm = Shared.sharedJvm
  lazy val dblib = Database.dblib
  lazy val server = Server.server
}
