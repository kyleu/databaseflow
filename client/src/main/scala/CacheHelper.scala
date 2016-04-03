import models.InitialState
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProcedureManager, _ }

trait CacheHelper { this: DatabaseFlow =>
  protected[this] def onInitialState(is: InitialState) = {
    UserManager.userId = Some(is.userId)
    UserManager.username = is.username
    UserManager.preferences = Some(is.preferences)

    MetadataManager.setSchema(is.schema, (key, name) => key match {
      case "table" => TableManager.tableDetail(name)
      case "view" => ViewManager.viewDetail(name)
      case "procedure" => ProcedureManager.procedureDetail(name)
    })

    $("#loading-panel").hide()

    performInitialAction()
  }
}
