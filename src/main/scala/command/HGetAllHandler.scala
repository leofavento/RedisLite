package command
import protocol.RespType
import store.InMemoryStore

class HGetAllHandler(store: InMemoryStore) extends CommandHandler {
  override def handle(args: List[RespType]): RespType = store.hgetall(args)
}
