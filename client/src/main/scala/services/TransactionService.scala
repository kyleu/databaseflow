package services

object TransactionService {
  def beginTransaction() = utils.Logging.info("TX Start!")
}
