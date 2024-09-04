package command
import protocol.RespType
import store.InMemoryStore

class HSetHandler(store: InMemoryStore) extends CommandHandler {
  override def handle(args: List[RespType]): RespType = store.hset(args)
}
