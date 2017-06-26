package models.sandbox

import enumeratum._
import utils.FutureUtils.defaultContext
import utils.{ApplicationContext, Logging}

import scala.concurrent.Future

sealed abstract class SandboxTask(val name: String, val description: String) extends EnumEntry with Logging {
  def run(app: ApplicationContext): Future[SandboxTask.Result] = {
    log.info(s"Running sandbox task [$this]...")
    val startMs = System.currentTimeMillis
    val result = call(app).map { r =>
      val res = SandboxTask.Result(this, "OK", r, (System.currentTimeMillis - startMs).toInt)
      log.info(s"Completed sandbox task [$this] with status [${res.status}] in [${res.elapsed}ms].")
      res
    }
    result
  }
  def call(app: ApplicationContext): Future[String]
}

object SandboxTask extends Enum[SandboxTask] {
  case class Result(task: SandboxTask, status: String = "OK", result: String, elapsed: Int)

  case object Metrics extends SandboxTask("Metrics Dump", "Lists all of the metrics for the running server.") {
    override def call(ctx: ApplicationContext) = {
      val url = "http://localhost:4261/metrics?pretty=true"
      val call = ctx.ws.url(url).withHttpHeaders("Accept" -> "application/json")
      call.get().map { json =>
        json.body
      }
    }
  }

  case object Testbed extends SandboxTask("Testbed", "A simple sandbox for messin' around.") {
    override def call(ctx: ApplicationContext) = Future.successful("Hello!")
  }

  case object MaintenanceToggle extends SandboxTask("Maintenance Mode", "Toggles the running state of the application.") {
    override def call(ctx: ApplicationContext) = {
      ApplicationContext.maintenanceMode = !ApplicationContext.maintenanceMode
      Future.successful("OK: " + ApplicationContext.maintenanceMode)
    }
  }

  override val values = findValues
}
