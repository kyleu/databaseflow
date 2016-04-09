package gui.ui

object RobotoFont {
  val font = {
    val fontStream = this.getClass.getClassLoader.getResourceAsStream("roboto.ttf")
    val f = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream)
    fontStream.close()
    f
  }

  val regularText = font.deriveFont(java.awt.Font.PLAIN, 18)
  val titleText = font.deriveFont(java.awt.Font.PLAIN, 36)
}
