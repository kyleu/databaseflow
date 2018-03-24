package services.scalaexport.db.file

import models.scalaexport.db.ExportModel
import models.scalaexport.db.config.ExportConfiguration
import models.scalaexport.file.GraphQLFile

object GraphQLQueryFile {
  def export(config: ExportConfiguration, model: ExportModel) = {
    val file = GraphQLFile(model.pkg, model.className)

    file.add(s"# Queries the system for ${model.plural}.")
    file.add(s"query ${model.className} {", 1)
    file.add(model.propertyName + "(", 1)
    file.add("q: null, # Or string literal")
    file.add("""filters: null, # Or filters of type `{ k: "", o: Equal, v: "" }`""")
    file.add("""orderBy: null, # Or orderBy of type `{ col: "", dir: Ascending }`""")
    file.add("limit: null, # Or number")
    file.add("offset: null # Or number")
    file.add(") {", -1)
    file.indent()

    file.add("totalCount")
    file.add("paging {", 1)
    file.add("current")
    file.add("next")
    file.add("itemsPerPage")
    file.add("}", -1)
    file.add("results {", 1)
    model.fields.foreach(f => file.add(f.propertyName))
    file.add("}", -1)
    file.add("durationMs")
    file.add("occurred")

    file.add("}", -1)
    file.add("}", -1)

    file
  }
}
