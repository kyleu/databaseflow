package ui

import org.scalajs.jquery.{ JQueryEventObject, jQuery => $ }
import utils.Logging

object SearchManager {
  private[this] lazy val searchContainer = $(".search-wrapper")
  private[this] lazy val searchInput = $("input#search", searchContainer)

  private[this] var currentSearch = ""

  def init() = {
    if (searchContainer.length != 1 || searchInput.length != 1) {
      throw new IllegalStateException("Missing search input field.")
    }
    searchInput.keyup { (e: JQueryEventObject) =>
      onTextChange(searchInput.value().toString)
    }
    searchInput.blur { (e: JQueryEventObject) =>
      onTextChange(searchInput.value().toString)
    }
  }

  def clearSearch() = {

  }

  def onTextChange(search: String) = {
    if (currentSearch == search) {
      Logging.info(s"Skipping search.")
    } else {
      Logging.info(s"Searching [$search]...")

      currentSearch = search
    }
  }
}
