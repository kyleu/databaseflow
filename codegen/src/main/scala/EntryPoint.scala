import models.codegen.Engine
import services.codegen.{ CodeGenerator, CapabilitiesProvider }

object EntryPoint {
  def main(args: Array[String]) {
    val ret = Engine.values.map(CapabilitiesProvider.capabilitiesFor)

    ret.foreach { e =>
      println(e.engine)
      println("  Column Types:")
      e.columnTypes.foreach(f => println(s"    ${f._1}: ${f._2.getOrElse("--")}"))
      println("  Built-in Functions:")
      e.builtInFunctions.foreach(f => println(s"    ${f.name}: ${f.typ}"))
    }

    CodeGenerator.go()
  }
}
