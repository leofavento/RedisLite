package command
import protocol.RespType
import store.InMemoryStore

class GetHandler(store: InMemoryStore) extends CommandHandler {
  override def handle(args: List[RespType]): RespType = store.get(args)
}
