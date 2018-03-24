package services.scalaexport.db.file

import models.scalaexport.db.{ExportField, ExportModel}
import models.scalaexport.file.JsonFile
import models.schema.ForeignKey

/*
  {
    "/admin/note": {
      "get": {
      },
      "post": {
      }
    },
    "/admin/note/{id}": {
      "get": {
        "summary": "View the Note entity matching the provided [id].",
        "operationId": "note.Note.view",
        "tags": [
          "note"
        ],
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "schema": {
              "type": "string",
              "example": "00000000-0000-0000-0000-000000000000"
            }
          }
        ],
        "responses": {
          "200": {
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/note.Note"
                }
              }
            }
          }
        }
      },
      "post": {
        "summary": "Updates the Note entity matching the provided [id] using the provided fields.",
        "operationId": "note.Note.edit",
        "tags": [
          "note"
        ],
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "schema": {
              "type": "string",
              "example": "00000000-0000-0000-0000-000000000000"
            }
          }
        ],
        "requestBody" : {
          "content": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/common.DataField"
            }
          }
        },
        "responses": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/note.Note"
              }
            }
          }
        }
      }
    },
    "/admin/note/{id}/remove": {
      "get": {
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "schema": {
              "type": "string",
              "example": "00000000-0000-0000-0000-000000000000"
            }
          }
        ],
        "responses": {
          "200": {
            "content": {
              "application/json": {
                "type": "array",
                "items": {
                  "$ref": "#/components/schemas/note.Note"
                }
              }
            }
          }
        }
      }
    }
  }
*/

object OpenApiPathsFile {

  def export(model: ExportModel, solo: Boolean = false) = {
    val file = JsonFile("paths" +: model.pkg, model.propertyName)
    file.add("{", 1)

    val route = if (solo) {
      s"/admin/${model.pkg.mkString("/")}"
    } else {
      s"/admin/${model.pkg.mkString("/")}/${model.propertyName}"
    }

    file.add(s""""$route": {""", 1)
    file.add("\"get\": {", 1)
    addList(model, file)
    file.add("},", -1)
    file.add("\"post\": {", 1)
    addCreate(model, file)
    file.add("}", -1)
    file.add("},", -1)

    if (model.pkFields.nonEmpty) {
      val pkPath = model.pkFields.map("{" + _.propertyName + "}").mkString("/")
      file.add(s""""$route/$pkPath": {""", 1)
      file.add("\"get\": {", 1)
      addView(model, file)
      file.add("},", -1)
      file.add("\"post\": {", 1)
      addEdit(model, file)
      file.add("}", -1)
      file.add("},", -1)
    }

    model.foreignKeys.foreach { fk =>
      addForeignKey(model, fk, file, route)
    }

    if (model.pkFields.nonEmpty) {
      val pkPath = model.pkFields.map("{" + _.propertyName + "}").mkString("/")
      file.add(s""""$route/$pkPath/remove\": {""", 1)
      file.add("\"get\": {", 1)
      addRemove(model, file)
      file.add("}", -1)
      file.add("}", -1)
    }

    file.add("}", -1)
    file
  }

  private[this] def addList(model: ExportModel, file: JsonFile) = {
    file.add("\"summary\": \"Queries and filters the system " + model.className + " entities.\",")
    file.add("\"operationId\": \"" + model.fullClassName + ".list\",")
    file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
    file.add("\"parameters\": [", 1)
    def addParam(k: String, last: Boolean = false) = {
      file.add("{", 1)
      file.add("\"$ref\": \"#/components/parameters/" + k + "\"")
      file.add("}" + (if (last) { "" } else { "," }), -1)
    }
    addParam("q")
    addParam("orderBy")
    addParam("orderAsc")
    addParam("limit")
    addParam("offset", last = true)
    file.add("],", -1)
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "Result\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
  }

  private[this] def addCreate(model: ExportModel, file: JsonFile) = {
    file.add("\"summary\": \"Creates a new " + model.className + " entity.\",")
    file.add("\"operationId\": \"" + model.fullClassName + ".create\",")
    file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
    file.add("\"requestBody\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"type\": \"array\",")
    file.add("\"items\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/common.DataField\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("},", -1)
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
  }

  private[this] def pkString(model: ExportModel) = model.pkFields.map(_.propertyName).mkString(", ")
  private[this] def addParams(fields: Seq[ExportField], file: JsonFile) = {
    file.add("\"parameters\": [", 1)
    fields.foreach { field =>
      file.add("{", 1)
      file.add("\"name\": \"" + field.propertyName + "\",")
      file.add("\"in\": \"path\",")
      file.add("\"schema\": {", 1)
      OpenApiPropertyHelper.contentFor(field.t, field.sqlTypeName, file)
      file.add("}", -1)
      file.add(if (fields.lastOption.contains(field)) { "}" } else { "}," }, -1)
    }
    file.add("],", -1)
  }

  private[this] def addView(model: ExportModel, file: JsonFile) = {
    file.add(s""""summary": "View the ${model.className} entity matching the provided [${pkString(model)}].",""")
    file.add("\"operationId\": \"" + model.fullClassName + ".view\",")
    file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
    addParams(model.pkFields, file)
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
  }

  private[this] def addEdit(model: ExportModel, file: JsonFile) = {
    file.add(s""""summary": "Updates the ${model.className} entity matching the provided [${pkString(model)}] using the provided fields.",""")
    file.add("\"operationId\": \"" + model.fullClassName + ".edit\",")
    file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
    addParams(model.pkFields, file)
    file.add("\"requestBody\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"type\": \"array\",")
    file.add("\"items\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/common.DataField\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("},", -1)
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
  }

  private[this] def addForeignKey(model: ExportModel, fk: ForeignKey, file: JsonFile, route: String) = fk.references match {
    case h :: Nil =>
      val field = model.fields.find(_.columnName == h.source).getOrElse(throw new IllegalStateException(s"Missing column [${h.source}]."))
      file.add(s""""$route/by${field.className}/{${field.propertyName}}": {""", 1)
      file.add("\"get\": {", 1)
      file.add(s""""summary": "Finds the ${model.className} entities associated to the provided [${field.propertyName}].",""")
      file.add("\"operationId\": \"" + model.fullClassName + ".by" + field.className + "\",")
      file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
      addParams(Seq(field), file)

      file.add("\"responses\": {", 1)
      file.add("\"200\": {", 1)
      file.add("\"content\": {", 1)
      file.add("\"application/json\": {", 1)
      file.add("\"schema\": {", 1)
      file.add("\"type\": \"array\",")
      file.add("\"items\": {", 1)
      file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "\"")
      file.add("}", -1)
      file.add("}", -1)
      file.add("}", -1)
      file.add("}", -1)
      file.add("}", -1)
      file.add("}", -1)

      file.add("}", -1)
      file.add(if (model.pkFields.isEmpty && model.foreignKeys.lastOption.contains(fk)) { "}" } else { "}," }, -1)
    case _ => //no op
  }

  private[this] def addRemove(model: ExportModel, file: JsonFile) = {
    file.add(s""""summary": "Removes the ${model.className} entity matching the provided [${pkString(model)}].",""")
    file.add("\"operationId\": \"" + model.fullClassName + ".remove\",")
    file.add("\"tags\": [\"" + model.pkg.mkString(".") + "\"],")
    addParams(model.pkFields, file)
    file.add("\"responses\": {", 1)
    file.add("\"200\": {", 1)
    file.add("\"content\": {", 1)
    file.add("\"application/json\": {", 1)
    file.add("\"schema\": {", 1)
    file.add("\"$ref\": \"#/components/schemas/" + model.fullClassName + "\"")
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
    file.add("}", -1)
  }
}
