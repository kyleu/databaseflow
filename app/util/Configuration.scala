package util

import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticatorSettings
import play.api.{Environment, Mode}

@javax.inject.Singleton
class Configuration @javax.inject.Inject() (val cnf: play.api.Configuration, env: Environment) {
  val debug = env.mode == Mode.Dev

  // Authentication
  val cookieAuthSettings = {
    import scala.concurrent.duration._
    val cfg = cnf.get[Option[play.api.Configuration]]("silhouette.authenticator.cookie").getOrElse {
      throw new IllegalArgumentException("Missing cookie configuration.")
    }

    CookieAuthenticatorSettings(
      cookieName = cfg.get[Option[String]]("name").getOrElse(throw new IllegalArgumentException()),
      cookiePath = cfg.get[Option[String]]("path").getOrElse(throw new IllegalArgumentException()),
      cookieDomain = None,
      secureCookie = cfg.get[Option[Boolean]]("secure").getOrElse(throw new IllegalArgumentException()),
      httpOnlyCookie = true,
      useFingerprinting = cfg.get[Option[Boolean]]("useFingerprinting").getOrElse(throw new IllegalArgumentException()),
      cookieMaxAge = cfg.get[Option[Int]]("maxAge").map(_.seconds),
      authenticatorIdleTimeout = cfg.get[Option[Int]]("idleTimeout").map(_.seconds),
      authenticatorExpiry = cfg.get[Option[Int]]("expiry").map(_.seconds).getOrElse(throw new IllegalArgumentException())
    )
  }
}
