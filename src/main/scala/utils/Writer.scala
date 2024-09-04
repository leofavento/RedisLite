package utils

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.io.Tcp.Write
import org.apache.pekko.util.ByteString
import protocol.{RespEncoder, RespType}

class Writer(writer: ActorRef) {
  def write(v: RespType): Unit = {
    val bytes = RespEncoder.encode(v)
    writer ! Write(ByteString(bytes))
  }
}
