package protocol

sealed trait RespType

case class RespString(value: String) extends RespType
case class RespError(value: String) extends RespType
case class RespBulkString(value: Option[String]) extends RespType
case class RespArray(value: List[RespType]) extends RespType
case class RespNull() extends RespType
case class RespInteger(value: Integer) extends RespType