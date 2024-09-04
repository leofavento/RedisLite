package actors

import org.apache.pekko
import pekko.actor.Actor
import pekko.io.Tcp.*
import protocol.{RespArray, RespBulkString, RespError, RespParser, RespType}
import storage.Aof
import command.{CommandHandler, CommandRegistry}
import utils.Writer

import java.io.BufferedInputStream
import java.io.IOException

class ConnectionHandler(aof: Aof) extends Actor {
  def receive: Receive = {
    case Received(data) =>
      val bufferedInputStream = new BufferedInputStream(data.iterator.asInputStream)
      val value = try {
        RespParser.parse(bufferedInputStream)
      } catch {
        case e: IOException =>
          println(s"Error processing request: ${e.getMessage}")
          RespError("error")
      }

      handleRequest(value)

    case PeerClosed =>
      println("Client disconnected")
      context.stop(self)
  }

  private def handleRequest(value: RespType): Unit = value match {
    case RespArray(elements) =>
      if (elements.nonEmpty) {
        elements.head match {
          case RespBulkString(Some(content)) =>
            val command = content.toUpperCase
            val args = elements.tail

            // Write to AOF if necessary
            if (command == "SET" || command == "HSET") {
              writeToAOF(value)
            }

            // Process the command
            val result = processCommand(command, args)
            sendResponse(result)

          case _ =>
            println("The first element is not a bulk string.")
        }
      } else {
        println("The array is empty.")
      }

    case _ => println("Invalid request, expected array.")
  }

  private def writeToAOF(value: RespType): Unit = {
    aof.write(value)
  }

  private def processCommand(command: String, args: List[RespType]): RespType = {
    CommandRegistry.getHandler(command) match {
      case Some(handler) => handler.handle(args)
      case None => RespError("Command not found")
    }
  }

  private def sendResponse(result: RespType): Unit = {
    val writer = new Writer(sender())
    writer.write(result)
  }
}
