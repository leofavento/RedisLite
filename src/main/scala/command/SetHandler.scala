package command
import protocol.RespType
import store.InMemoryStore

class SetHandler(store: InMemoryStore) extends CommandHandler {
  override def handle(args: List[RespType]): RespType = store.set(args)
}
