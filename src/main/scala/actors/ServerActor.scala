package actors

import org.apache.pekko
import org.apache.pekko.io.Tcp.*
import pekko.actor.{Actor, Props}
import pekko.io.{IO, Tcp}
import storage.Aof

import java.net.InetSocketAddress

class ServerActor(aof: Aof) extends Actor {
  import context.system

  // Start listening on the specified port
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6379))
  println("Listening on port :6379")

  def receive: Receive = {
    case Bound(localAddress) =>
      println(s"Server bound to $localAddress")

    case CommandFailed(_: Bind) =>
      println("Bind failed, stopping server")
      context.stop(self)

    case Connected(remote, _) =>
      println(s"Client connected from $remote")
      val handler = context.actorOf(Props(new ConnectionHandler(aof)))
      val connection = sender()
      connection ! Register(handler)
  }
}
