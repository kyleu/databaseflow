import java.nio.file.{ Files, Path, Paths }
import scala.sys.process._

object IconCreator extends App {
  private[this] val icons = Seq("icon")
  private[this] val colors = Seq(
    "black" -> "#000000"
  )

  val srcDir = Paths.get(".", "util", "iconCreator", "src", "main", "resources", "svg")
  val tmpDir = Paths.get(".", "tmp", "icons")
  Files.createDirectories(tmpDir)
  val outDir = Paths.get(".", "out")

  private val startMs = System.currentTimeMillis
  println("Creating icons...")
  go()
  println(s"Icon creation complete in [${System.currentTimeMillis - startMs}ms].")

  def go() = {
    wipeTarget()

    for (color <- colors) {
      for (icon <- icons) {
        val src = srcDir.resolve(icon + ".svg")
        val tgt = tmpDir.resolve(s"$icon-${color._1}.svg")
        colorizeSvg(src, tgt, color._2)
        convert(icon, color._1)
      }
    }
  }

  private[this] def colorizeSvg(src: Path, tgt: Path, colorHex: String) = {
    val lines = readFileAsString(src)
    val output = lines.map { line =>
      if (line.startsWith("\t<path")) {
        line.replaceAllLiterally(
          "\t<path",
          s"""\t<path stroke="none" stroke-opacity="0.0" fill="$colorHex" fill-opacity="1.0""""
        )
      } else {
        line
      }
    }.mkString("\n")
    writeFile(tgt, output)
  }

  private[this] def convert(icon: String, color: String) = {
    println(s"Converting [$icon:$color]")
    s"convert -resize 500x500 -background none ./tmp/icons/$icon-$color.svg ./tmp/icons/$icon-$color-500.png".!
    s"convert -resize 64x64 -background none ./tmp/icons/$icon-$color.svg ./tmp/icons/$icon-$color@2x.png".!
    s"convert -resize 32x32 -background none ./tmp/icons/$icon-$color.svg ./tmp/icons/$icon-$color.png".!
  }

  private[this] def wipeTarget() = {
    val tgtStream = Files.newDirectoryStream(tmpDir)
    val tgtItr = collection.JavaConverters.asScalaIteratorConverter(tgtStream.iterator()).asScala
    for (p <- tgtItr) {
      Files.delete(p)
    }
  }

  private def readFileAsString(p: Path) = collection.JavaConverters.asScalaIteratorConverter(Files.readAllLines(p).iterator).asScala.toSeq
  private def writeFile(p: Path, s: String) = Files.write(p, s.getBytes)
}
