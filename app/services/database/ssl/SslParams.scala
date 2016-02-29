package services.database.ssl

import java.security.KeyStore

case class SslParams(identityStore: KeyStore, identityStorePassword: String, trustStore: KeyStore)
