object Entrypoint extends App {
  println("License Generator")

  KeyGenerator.init()
  LicenseGenerator.init()

  KeyGenerator.generateKeys()
  LicenseGenerator.writeTestLicense()
  LicenseGenerator.readTestLicense()
}
