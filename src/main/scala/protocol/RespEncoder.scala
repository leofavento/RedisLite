package protocol

object RespEncoder {
  def encode(resp: RespType): Array[Byte] = resp match
    case RespString(value) => s"+$value\r\n".getBytes("UTF-8")

    case RespError(value) => s"-$value\r\n".getBytes("UTF-8")

    case RespBulkString(Some(value)) => s"$$${value.length}\r\n$value\r\n".getBytes("UTF-8")

    case RespArray(values) =>
      val encodedArray = values.foldLeft(Array[Byte]())(_ ++ encode(_))
      s"*${values.length}\r\n".getBytes("UTF-8") ++ encodedArray

    case RespNull() => "$-1\r\n".getBytes("UTF-8")

    case protocol.RespBulkString(None) => "$-1\r\n".getBytes("UTF-8")

    case protocol.RespInteger(value) =>s":$value\r\n".getBytes("UTF-8")
}
