package command

import protocol.{RespType, RespBulkString, RespString}

class PingHandler extends CommandHandler {
  override def handle(args: List[RespType]): RespType = {
    args.headOption match {
      case Some(RespBulkString(Some(value))) if value.isInstanceOf[String] =>
        RespString(value)
      case _ =>
        RespString("PONG")
    }
  }
}
