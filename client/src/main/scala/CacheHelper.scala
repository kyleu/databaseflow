import models.InitialState
import org.scalajs.jquery.{ jQuery => $ }
import ui.{ MetadataManager, UserManager }

trait CacheHelper { this: DatabaseFlow =>
  protected[this] def onInitialState(is: InitialState) = {
    UserManager.userId = is.userId
    UserManager.username = is.username
    UserManager.preferences = is.preferences

    MetadataManager.updateSchema(is.schema)

    $("#loading-panel").hide()

    performInitialAction()
  }
}
