package keys

import net.nicholaswilliams.java.licensing.encryption.PasswordProvider

object PrivatePasswordProvider extends PasswordProvider {
  override val getPassword = Array(
    0x00000069, 0x00000067, 0x0000006F, 0x00000074, 0x0000006D, 0x00000061, 0x00000064, 0x0000006D,
    0x00000061, 0x00000064, 0x00000066, 0x0000006C, 0x0000006F, 0x00000077
  ).map(_.toChar)
}
