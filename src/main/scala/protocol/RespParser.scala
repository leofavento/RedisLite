package protocol

import java.io.{BufferedInputStream, IOException}
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

object RespParser {
  def parse(reader: BufferedInputStream): RespType = {
    val _type = reader.read()
    if (_type == -1) {
      throw new IOException("End of stream reached")
    }
    _type.toChar match {
      case '*' => parseArray(reader)
      case '$' => parseBulkString(reader)
      case ':' => parseInteger(reader)
      case other => throw new IOException(s"Unknown type: $other")
    }
  }

  private def parseString(reader: BufferedInputStream): RespString = {
    val line = readLine(reader)._1
    RespString(new String(line))
  }

  private def parseError(reader: BufferedInputStream): RespError = {
    val line = readLine(reader)._1
    RespError(new String(line))
  }

  private def parseBulkString(reader: BufferedInputStream): RespBulkString = {
    val (length, _) = readInteger(reader)
    if (length == -1) {
      RespBulkString(None) // Handle null bulk strings
    } else {
      val bulk = new Array[Byte](length)
      reader.read(bulk, 0, length)
      readLine(reader) // Consume the trailing \r\n
      RespBulkString(Some(new String(bulk)))
    }
  }

  private def parseArray(reader: BufferedInputStream): RespArray = {
    val (length, _) = readInteger(reader)
    if (length == -1) {
      RespArray(Nil) // Handle null arrays as empty lists
    } else {
      val elements = for (_ <- 0 until length) yield parse(reader)
      RespArray(elements.toList)
    }
  }

  private def parseInteger(reader: BufferedInputStream): RespInteger = {
    val line = readLine(reader)._1
    val lineStr = new String(line, "UTF-8")
    Try(lineStr.toInt) match {
      case Success(value) => RespInteger(value)
      case Failure(_) => throw new IOException(s"Failed to parse integer from line: $lineStr")
    }
  }

  private def readLine(reader: BufferedInputStream): (Array[Byte], Int) = {
    val lineBuffer = ArrayBuffer[Byte]()
    var n = 0
    var b = 0

    while (true) {
      b = reader.read()
      if (b == -1) {
        throw new IOException("End of stream reached")
      }
      n += 1
      lineBuffer.append(b.toByte)

      // Check for \r\n
      if (lineBuffer.length >= 2 && lineBuffer(lineBuffer.length - 2) == '\r') {
        // Remove the \r\n before returning
        lineBuffer.dropRightInPlace(2)
        return (lineBuffer.toArray, n)
      }
    }
    // Shouldn't reach here, but just in case
    (lineBuffer.toArray, n)
  }

  private def readInteger(reader: BufferedInputStream): (Int, Int) = {
    val (line, n) = readLine(reader)
    val lineStr = new String(line)
    Try(lineStr.toInt) match {
      case Success(value) => (value, n)
      case Failure(_) => throw new IOException(s"Failed to parse integer from line: $lineStr")
    }
  }
}
