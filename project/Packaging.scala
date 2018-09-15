import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.Debian
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.{ Docker, dockerExposedPorts, dockerExposedVolumes }
import com.typesafe.sbt.packager.jdkpackager.JDKPackagerPlugin.autoImport.{ JDKPackager, jdkAppIcon, jdkPackagerBasename }
import sbt.Keys._
import sbt._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{ Universal, useNativeZip }

object Packaging {
  private[this] lazy val iconGlob = sys.props("os.name").toLowerCase match {
    case os if os.contains("mac") => "*.icns"
    case os if os.contains("win") => "*.ico"
    case _ => "*.png"
  }

  private[this] def isConf(x: (File, String)) = x._1.getAbsolutePath.contains("conf/")

  val settings = useNativeZip ++ Seq(
    topLevelDirectory in Universal := None,
    packageSummary := description.value,
    packageDescription := description.value,

    mappings in Universal := (mappings in Universal).value.filterNot(isConf),

    // Linux Settings
    packageDescription in Debian := "Database Flow Debian Package - A modern SQL client. https://databaseflow.com",
    topLevelDirectory in Debian := Some(Shared.projectId + "-" + Shared.Versions.app),
    debianNativeBuildOptions in Debian := Seq("-Zgzip", "-z3"),
    debianPackageRecommends in Debian ++= Seq("openjdk-8-jre"),
    rpmVendor := "Database Flow",
    linuxPackageMappings := linuxPackageMappings.value.filter(_.fileData.config != "false"),

    // Docker
    dockerExposedPorts := Seq(4260, 4261, 4262, 4263),
    defaultLinuxInstallLocation in Docker := "/opt/databaseflow",
    packageName in Docker := packageName.value,
    dockerExposedVolumes := Seq("/opt/databaseflow"),
    version in Docker := version.value,

    // JDK Packager
    jdkAppIcon :=  (sourceDirectory.value ** iconGlob).getPaths.headOption.map(file),
    jdkPackagerBasename := "DatabaseFlow",
    name in JDKPackager := "Database Flow",

    javaOptions in Universal ++= Seq(
      "-J-Xmx2g",
      "-J-Xms256m",
      "-Dproject=databaseflow"
    )
  )
}
