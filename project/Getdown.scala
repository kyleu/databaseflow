import sbt.Keys._
import sbt._
import com.threerings.getdown.tools.Digester

object Getdown {
  val cmd = TaskKey[Unit]("getdown", "Generates Getdown client")

  def task = (sourceDirectory in Compile, target, streams, baseDirectory) map {
    (srcDir, target, s, dir) =>

      val dest = (dir / "build" / "getdown").asFile
      def copy (files: Iterable[File]) = IO.copy(files.map(src => (src.asFile, dest / src.getName)))

      copy((dir / "src" / "deploy" / "getdown" * "*").get)

      val libDir = target / "universal" / "stage" / "lib" * "*"
      val libs = libDir.get.map { lib =>
        IO.copy(Seq((lib, dest / "lib" / lib.getName)))
        "lib/" + lib.getName
      }

      val binDir = target / "universal" / "stage" / "bin" * "*"
      val bins = binDir.get.map { bin =>
        IO.copy(Seq((bin, dest / "bin" / bin.getName)))
        "bin/" + bin.getName
      }

      val readme = target / "universal" / "stage" / "README.md"
      IO.copy(Seq((readme, dest / readme.getName)))

      val deps = (readme.getName +: bins) ++ libs
      val output = deps.map("code = " + _).mkString("\n")

      val destConfig = dest / "getdown.txt"
      val cfgTxt = IO.read(destConfig)
      val newCfgTxt = cfgTxt.replace("{{code}}", output)
      IO.write(destConfig, newCfgTxt)

      try {
        Digester.createDigest(dest)
      } catch {
        case e: Throwable =>
          s.log.warn("Digest creation failed " + e)
          throw e
      }
  }
}
