package models.auth

import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.actions.{ SecuredErrorHandler, UnsecuredErrorHandler }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.impl.authenticators.{ CookieAuthenticator, CookieAuthenticatorService }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import com.mohiva.play.silhouette.impl.util.{ DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator }
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.WSClient
import services.user.{ PasswordInfoService, UserSearchService }
import utils.Config

class AuthModule extends AbstractModule with ScalaModule {
  override def configure() = {
    bind[Silhouette[AuthEnv]].to[SilhouetteProvider[AuthEnv]]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    //bind[].to[]
  }

  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  @Provides
  def provideEnvironment(
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus,
    userSearchService: UserSearchService
  ): Environment[AuthEnv] = {
    Environment[AuthEnv](userSearchService, authenticatorService, Seq(), eventBus)
  }

  @Provides
  def provideAuthenticatorService(fpg: FingerprintGenerator, idg: IDGenerator, config: Config, clock: Clock): AuthenticatorService[CookieAuthenticator] = {
    new CookieAuthenticatorService(config.cookieAuthSettings, None, fpg, idg, clock)
  }

  // @Provides
  // def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  @Provides
  def provideCredentialsProvider(authInfoRepository: AuthInfoRepository, passwordHasher: PasswordHasher): CredentialsProvider = {
    new CredentialsProvider(authInfoRepository, passwordHasher, Seq(passwordHasher))
  }

  @Provides
  def provideAuthInfoRepository(passwordInfoService: PasswordInfoService): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoService)
  }
}
