package services.logging

import better.files._
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

import scala.io.Source

object LogService {
  var enabled = true

  private[this] val logStartPhrases = LogLevel.values.map(_.startPhrase)

  val logDir = "logs".toFile
  if (!logDir.exists) {
    throw new IllegalStateException("Log directory does not exist.")
  }

  def listFiles() = logDir.children.filter(_.name.endsWith(".log")).toSeq.sortBy(_.name).map { f =>
    f.name -> f.size.toInt
  }

  def getLogs(name: String) = {
    val ret = collection.mutable.ArrayBuffer.empty[ServerLog]
    var pendingLog: Option[ServerLog] = None
    getLines(name).zipWithIndex.foreach {
      case blank if blank._1.isEmpty => // noop, ignore
      case main if logStartPhrases.contains(main._1.take(7).trim) =>
        pendingLog match {
          case Some(log) => ret += log
          case None => // no op, ignore
        }
        pendingLog = Some(parseLog(main._1, main._2))
      case l => pendingLog match {
        case Some(log) => pendingLog = Some(log.copy(message = log.message + { if (log.message.isEmpty) { "" } else { "\n" } } + l._1))
        case None => throw new IllegalStateException(s"${logStartPhrases.mkString(", ")}\nUnhandled line [${l._2}] with no pending log: [${l._1}].")
      }
    }
    pendingLog.foreach(ret += _)
    ret
  }

  private[this] def getLines(name: String) = (logDir / name).lines().toSeq

  private[this] val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS")

  private[this] def parseLog(line: String, lineNumber: Int) = {
    // [ERROR] 2015-07-23 18:48:56,395 from application in New I/O worker #29

    val levelEndIndex = line.indexOf(']')
    if (levelEndIndex == -1 || (levelEndIndex != 5 && levelEndIndex != 6)) {
      throw new IllegalStateException(s"Invalid main line index [$levelEndIndex] for line [$lineNumber]: $line")
    }
    val capLevel = line.substring(1, levelEndIndex)
    val level = LogLevel.withName(capLevel.head + capLevel.tail.toLowerCase)

    val occurredEndIndex = line.indexOf(' ', levelEndIndex + 20)
    val occurredString = line.substring(levelEndIndex + 2, occurredEndIndex)
    val occurred = LocalDateTime.parse(occurredString, dateFormatter)

    val loggerStartIndex = line.indexOf(" from ", occurredEndIndex) + 6
    if (loggerStartIndex == 5) {
      throw new IllegalStateException(s"No [from] section in line [$lineNumber}]: $line")
    }
    val loggerEndIndex = line.indexOf(" in ", loggerStartIndex)
    val logger = line.substring(loggerStartIndex, loggerEndIndex)

    val thread = line.substring(loggerEndIndex + 4)

    ServerLog(level, lineNumber, logger, thread, "", occurred)
  }
}
