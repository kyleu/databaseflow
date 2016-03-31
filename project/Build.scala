import sbt._
import com.typesafe.sbt.packager.universal.UniversalKeys

object Build extends Build with UniversalKeys {
  lazy val sharedJs = Shared.sharedJs
  lazy val client = Client.client

  lazy val codegen = CodeGen.codegen

  lazy val sharedJvm = Shared.sharedJvm
  lazy val dblibs = Database.dblibs
  lazy val server = Server.server
}
