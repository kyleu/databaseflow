import services.codegen.CodeGenerator

object EntryPoint {
  def main(args: Array[String]): Unit = {
    if (args.nonEmpty) {
      throw new IllegalArgumentException("No arguments are accepted.")
    }
    CodeGenerator.go()
  }
}
