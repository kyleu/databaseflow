package utils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSBracketAccess

object Messages {
  @js.native
  trait MessageObject extends js.Object {
    @JSBracketAccess
    def apply(key: String): String = js.native
  }

  lazy val jsMessages = {
    val ret = js.Dynamic.global.messages.asInstanceOf[MessageObject]
    if (ret == None.orNull) {
      throw new IllegalStateException("Missing localization object [messages].")
    }
    ret
  }

  def apply(s: String, args: Any*) = {
    //utils.Logging.info(s)
    val msg = jsMessages(s)
    if (args.isEmpty) {
      msg
    } else {
      throw new IllegalStateException("Can't handle arguments.")
    }
  }
}
