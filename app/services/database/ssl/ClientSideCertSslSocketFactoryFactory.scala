package services.database.ssl

import java.net.{Socket, InetAddress}
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl._

object ClientSideCertSslSocketFactoryFactory {
  val configs = new ConcurrentHashMap[String, SslParams]

  def configure(param: String, params: SslParams) {
    configs.put(param, params)
  }

  def factory(param: String): SSLSocketFactory = {
    val params = configs.get(param)
    if (params == null) {
      throw new IllegalArgumentException(s"Unknown ssl socket factory params [$param].")
    }

    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    keyManagerFactory.init(params.identityStore, params.identityStorePassword.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    trustManagerFactory.init(params.trustStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, null)
    context.getSocketFactory
  }
}

class ClientSideCertSslSocketFactoryFactory(param: String) extends SSLSocketFactory {
  val delegate = ClientSideCertSslSocketFactoryFactory.factory(param)

  def getDefaultCipherSuites: Array[String] = delegate.getDefaultCipherSuites
  def getSupportedCipherSuites: Array[String] = delegate.getSupportedCipherSuites
  def createSocket(p1: Socket, p2: String, p3: Int, p4: Boolean): Socket = delegate.createSocket(p1, p2, p3, p4)
  def createSocket(p1: String, p2: Int): Socket = delegate.createSocket(p1, p2)
  def createSocket(p1: String, p2: Int, p3: InetAddress, p4: Int): Socket = delegate.createSocket(p1, p2, p3, p4)
  def createSocket(p1: InetAddress, p2: Int): Socket = delegate.createSocket(p1, p2)
  def createSocket(p1: InetAddress, p2: Int, p3: InetAddress, p4: Int): Socket = delegate.createSocket(p1, p2, p3, p4)
}
