package command

import protocol.RespType

trait CommandHandler {
  def handle(args: List[RespType]): RespType
}
