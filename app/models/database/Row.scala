package models.database

import java.sql.{ SQLException, ResultSet }

object Row {
  class Iter(rs: ResultSet) extends Iterator[Row] {
    private[this] val row = new Row(rs)
    private[this] var advanced = false
    private[this] var canAdvance = false

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
    val colNames = colRange.map(md.getColumnName)
    val colValues = colRange.map(rs.getObject)
    colNames.zip(colValues).toMap
  }

  def asOpt[T](idx: Int): Option[T] = extractOpt(rs.getObject(idx))
  def asOpt[T](key: String): Option[T] = extractOpt(rs.getObject(key))

  def as[T](idx: Int): T = asOpt(idx).getOrElse(throw new IllegalArgumentException(s"Column [$idx] is null."))
  def as[T](key: String): T = asOpt(key).getOrElse(throw new IllegalArgumentException(s"Column [$key] is null."))

  def asArray[T: reflect.ClassTag](index: Int): Option[Array[T]] = extractArray[T](rs.getArray(index + 1))
  def asArray[T: reflect.ClassTag](name: String): Option[Array[T]] = extractArray[T](rs.getArray(name))

  private[this] def extractOpt[T](x: AnyRef) = x match {
    case _ if rs.wasNull => None
    case o => Option(o).map(_.asInstanceOf[T])
  }

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
