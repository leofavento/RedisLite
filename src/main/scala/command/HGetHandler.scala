package command
import protocol.RespType
import store.InMemoryStore

class HGetHandler(store: InMemoryStore) extends CommandHandler {
  override def handle(args: List[RespType]): RespType = store.hget(args)
}
