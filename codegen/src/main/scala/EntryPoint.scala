import services.codegen.CodeGenerator

object EntryPoint {
  def main(args: Array[String]) {
    if (args.nonEmpty) {
      throw new IllegalArgumentException("No arguments are accepted.")
    }
    CodeGenerator.go()
  }
}
