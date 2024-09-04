package storage

import protocol.{RespEncoder, RespParser, RespType}

import java.io.{BufferedInputStream, FileInputStream, FileOutputStream, IOException}
import java.util.concurrent.{Executors, TimeUnit}

class Aof(path: String) {
  private val fileOutputStream = new FileOutputStream(path, true)
  private val scheduler = Executors.newSingleThreadScheduledExecutor()

  scheduler.scheduleAtFixedRate(
    () => {
      try {
        fileOutputStream.getFD.sync()
      } catch {
        case e: IOException => println(s"Error syncing file $path: ${e.getMessage}")
      }
    },
    1,
    1,
    TimeUnit.SECONDS
  )

  def write(value: RespType): Unit = {
    try {
      val bytes = RespEncoder.encode(value)
      fileOutputStream.write(bytes)
    } catch {
      case e: IOException => println(s"Error writing to file $path: ${e.getMessage}")
    }
  }

  def read(): List[RespType] = {
    val fileInputStream = new FileInputStream(path)
    val bufferedInputStream = new BufferedInputStream(fileInputStream)
    try {
      val results = scala.collection.mutable.ArrayBuffer[RespType]()
      var continueReading = true
      while (continueReading) {
        try {
          val value = RespParser.parse(bufferedInputStream)
          value match {
            case null => continueReading = false
            case _ => results += value
          }
        } catch {
          case e: IOException =>
            continueReading = false
        }
      }
      results.toList
    } finally {
      bufferedInputStream.close()
      fileInputStream.close()
    }
  }

  def close(): Unit = {
    try {
      fileOutputStream.close()
    } catch {
      case e: IOException => println(s"Error closing file: ${e.getMessage}")
    } finally {
      scheduler.shutdown()
      try {
        scheduler.awaitTermination(1, TimeUnit.SECONDS)
      } catch {
        case e: InterruptedException => println(s"Scheduler termination interrupted: ${e.getMessage}")
      }
    }
  }
}
