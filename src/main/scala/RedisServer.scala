import actors.ServerActor
import command.CommandRegistry
import org.apache.pekko.actor.{ActorSystem, Props}
import storage.Aof
import protocol.{RespArray, RespBulkString, RespType}

object RedisServer extends App {
  implicit val system: ActorSystem = ActorSystem("RedisServerSystem")

  private val aof = new Aof("database.aof")
  loadAof()

  system.actorOf(Props(new ServerActor(aof)), "ServerActor")

  private def loadAof(): Unit = {
    val valueSeq = aof.read()
    valueSeq.foreach {
      case RespArray(elements) =>
        if (elements.nonEmpty) {
          elements.head match {
            case RespBulkString(Some(command)) =>
              val args = elements.tail

              CommandRegistry.getHandler(command) match {
                case Some(handler) => handler.handle(args)
                case None => println("Failed to load Aof.")
              }
            case _ =>
              println("Failed to load Aof.")
          }
        } else {
          println("Failed to load Aof.")
        }
      case _ =>
        println("Failed to load Aof.")
    }
  }
}
