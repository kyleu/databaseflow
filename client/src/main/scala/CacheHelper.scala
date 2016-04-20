import models.InitialState
import ui.UserManager

trait CacheHelper { this: DatabaseFlow =>
  protected[this] def onInitialState(is: InitialState) = {
    UserManager.userId = is.userId
    UserManager.username = is.username
    UserManager.preferences = is.preferences
  }
}
