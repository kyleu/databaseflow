package services.payment

import com.stripe.Stripe
import com.stripe.model.Charge
import com.stripe.net.RequestOptions.RequestOptionsBuilder
import licensing.LicenseEdition
import utils.Logging

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

object StripePaymentService extends Logging {
  private[this] val requestOptions = new RequestOptionsBuilder().build()

  var jsKey = ""
  var prices = Map.empty[LicenseEdition, Int]

  def init(sk: String, pk: String, personalPrice: Int, teamPrice: Int) = {
    Stripe.apiKey = sk
    jsKey = pk
    prices = Map(
      LicenseEdition.NonCommercial -> 0,
      LicenseEdition.Personal -> personalPrice,
      LicenseEdition.Team -> teamPrice
    )
  }

  case class StripeToken(token: String, tokenType: String, email: Option[String], amount: Long) {
    def validate() = {
      if (token.isEmpty) {
        throw new IllegalArgumentException("Missing token.")
      }
      if (tokenType.isEmpty) {
        throw new IllegalArgumentException("Missing token type.")
      }
      if (amount == 0) {
        throw new IllegalArgumentException("Invalid amount.")
      }
    }
  }

  private[this] def fix(params: Map[String, AnyRef]): java.util.Map[String, AnyRef] = params.map {
    case (k, v: Map[String @unchecked, AnyRef @unchecked]) => k -> fix(v)
    case x => x
  }.asJava

  def createCharge(chargeParams: Map[String, AnyRef]) = try {
    Right(Charge.create(fix(chargeParams), requestOptions))
  } catch {
    case NonFatal(x) => Left(x)
  }

  def redeemToken(description: String, token: StripeToken) = {
    val description = "Team Edition License"

    val params = Map[String, AnyRef](
      "amount" -> Long.box(token.amount),
      "currency" -> "usd",
      "customer" -> token.email.getOrElse("Unknown User"),
      "description" -> description
    )
    log.info(s"Redeeming [$token].")

    createCharge(params)
  }
}
