package models.database

import java.sql.ResultSet

object Row {
  class Iter(rs: ResultSet) extends Iterator[Row] {
    private val row = new Row(rs)
    private var advanced, canAdvance = false

    def hasNext = {
      if (!advanced) {
        advanced = true
        canAdvance = rs.next()
      }
      canAdvance
    }

    def next() = if (hasNext) {
      advanced = false
      row
    } else {
      Iterator.empty.next()
    }
  }
}

class Row(val rs: ResultSet) {
  lazy val toMap = {
    val md = rs.getMetaData
    val colRange = 1 until (1 + md.getColumnCount)
    val colNames =  colRange.map(md.getColumnName)
    val colValues = colRange.map(rs.getObject)
    colNames.zip(colValues).toMap
  }

  def getObject(idx: Int): AnyRef = rs.getObject(idx)
  def getObject(key: String): AnyRef = rs.getObject(key)

  private[this] def extract[A](f: A): Option[A] = if (rs.wasNull()) None else Some(f)

  def array[T: reflect.ClassTag](index: Int): Option[Array[T]] = extractArray[T](rs.getArray(index + 1))

  def array[T: reflect.ClassTag](name: String): Option[Array[T]] = extractArray[T](rs.getArray(name))

  private[this] def extractArray[T: reflect.ClassTag](sqlArray: java.sql.Array): Option[Array[T]] = {
    if (rs.wasNull()) {
      None
    } else {
      Option(sqlArray.getArray
        .asInstanceOf[Array[Object]]
        .map(_.asInstanceOf[T]))
    }
  }
}
