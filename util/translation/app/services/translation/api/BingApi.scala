package services.translation.api

import play.api.libs.ws.WSClient

import scala.concurrent.Future

@javax.inject.Singleton
class BingApi @javax.inject.Inject() (ws: WSClient) extends ApiProvider("Yandex") {
  val apiKey = ""
  def url(lang: String) = s"?key=$apiKey&lang=en-$lang"

  override def translate(lang: String, key: String, text: String) = {
    Future.successful(None)
  }
}
