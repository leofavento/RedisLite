package command

import store.InMemoryStore

object CommandRegistry {
  private val store = InMemoryStore()

  val handlers: Map[String, CommandHandler] = Map(
    "PING" -> new PingHandler,
    "SET" -> new SetHandler(store),
    "GET" -> new GetHandler(store),
    "HSET" -> new HSetHandler(store),
    "HGET" -> new HGetHandler(store),
    "HGETALL" -> new HGetAllHandler(store)
  )

  def getHandler(command: String): Option[CommandHandler] = handlers.get(command.toUpperCase())
}
