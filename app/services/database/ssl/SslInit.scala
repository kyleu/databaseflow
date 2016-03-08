package services.database.ssl

import java.io.FileInputStream
import java.security.KeyStore
import java.util.UUID

import utils.NullUtils

object SslInit {
  def initSsl(ssl: SslSettings): Map[String, String] = {
    val clientCertKeyStoreProvider = ssl.clientCertKeyStoreProvider.getOrElse(KeyStore.getDefaultType)
    val clientCertKeyStorePassword = ssl.clientCertKeyStorePassword.map(_.toCharArray).orNull
    val clientCertKeyStoreStream = new FileInputStream(ssl.clientCertKeyStorePath)
    val clientCertKeyStore = KeyStore.getInstance(clientCertKeyStoreProvider)

    val trustKeyStoreProvider = ssl.trustKeyStoreProvider.getOrElse(KeyStore.getDefaultType)
    val trustKeyStoreStream = new FileInputStream(ssl.trustKeyStoreProviderPath)
    val trustKeyStore = KeyStore.getInstance(trustKeyStoreProvider)

    clientCertKeyStore.load(clientCertKeyStoreStream, clientCertKeyStorePassword)
    trustKeyStore.load(trustKeyStoreStream, NullUtils.inst)

    val identifier = UUID.randomUUID().toString
    val sslParams = SslParams(
      clientCertKeyStore,
      ssl.clientCertKeyStorePassword.orNull,
      trustKeyStore
    )

    ClientSideCertSslSockets.configure(identifier, sslParams)

    Map(
      "ssl" -> "true",
      "sslfactory" -> "services.database.ssl.ClientSideCertSslSockets",
      "sslfactoryarg" -> identifier
    )
  }
}
