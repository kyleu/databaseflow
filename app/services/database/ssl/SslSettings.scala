package services.database.ssl

case class SslSettings(
    clientCertKeyStorePath: String,
    trustKeyStoreProviderPath: String,
    clientCertKeyStorePassword: Option[String],
    clientCertKeyStoreProvider: Option[String] = None,
    trustKeyStoreProvider: Option[String] = None
)
