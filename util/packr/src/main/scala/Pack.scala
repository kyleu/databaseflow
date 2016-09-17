import com.badlogicgames.packr._

object Pack {
  private[this] val inputDir = "target/universal/stage/"
  private[this] val outputDir = new java.io.File("build/packr")

  def main(args: Array[String]): Unit = {
    run()
  }

  def run() = {
    val config = new PackrConfig()
    config.platform = PackrConfig.Platform.Windows64
    config.jdk = "/Users/kyle/Downloads/java-1.8.0-openjdk-1.8.0.101-1-ojdkbuild.b13.windows.x86_64.zip"
    config.executable = "DatabaseFlow"
    config.classpath = java.util.Arrays.asList(
      inputDir + "lib/databaseflow.databaseflow-1.0.0.jar"
    )
    config.mainClass = "DatabaseFlow"
    config.vmArgs = java.util.Arrays.asList("Xmx1G")
    //config.minimizeJre = "soft"
    config.outDir = outputDir

    new Packr().pack(config)
  }
}
