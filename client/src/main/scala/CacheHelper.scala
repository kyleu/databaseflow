import models.InitialState
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ ProcedureManager, _ }

trait CacheHelper { this: DatabaseFlow =>
  protected[this] def onInitialState(is: InitialState) = {
    UserManager.userId = Some(is.userId)
    UserManager.username = is.username
    UserManager.preferences = Some(is.preferences)

    MetadataManager.updateSchema(is.schema)

    $("#loading-panel").hide()

    performInitialAction()
  }
}
