import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin.autoImport._
import sbt.Keys._
import sbt._

object Packaging {
  private[this] lazy val iconGlob = sys.props("os.name").toLowerCase match {
    case os if os.contains("mac") => "*.icns"
    case os if os.contains("win") => "*.ico"
    case _ => "*.png"
  }

  val soloSettings = Seq(
    mainClass in Compile := Some("DatabaseFlow"),

    topLevelDirectory := Some("database-flow"),
    packageSummary := description.value,
    packageDescription := "Database Flow helps you do all sorts of cool stuff with your database.",
    rpmVendor := "Database Flow",
    wixProductId := "5fee44ae-0989-429b-9b1a-de8ec7dd9af5",
    wixProductUpgradeId := "6d353c6a-6f39-48f1-afa8-2c5eb726a8b8",
    jdkAppIcon := (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file),
    jdkPackagerType := "installer",
    jdkPackagerJVMArgs := Seq("-Xmx2g"),
    jdkPackagerToolkit := SwingToolkit,
    jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)
  )

  val teamSettings = Seq(
    topLevelDirectory := Some("database-flow"),
    packageSummary := description.value,
    packageDescription := "Database Flow helps you do all sorts of cool stuff with your database."
  )
}
