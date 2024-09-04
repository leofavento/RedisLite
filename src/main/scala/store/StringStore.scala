package store

import protocol.{RespBulkString, RespError, RespNull, RespString, RespType}

import java.util.concurrent.ConcurrentHashMap

class StringStore {
  private val store = ConcurrentHashMap[String, String]()

  def set(args: List[RespType]): RespType = args match {
    case List(RespBulkString(Some(key)), RespBulkString(Some(value))) =>
      store.put(key, value)
      RespString("OK")
    case _ =>
      RespError("ERR wrong number of arguments for 'set' command")
  }

  def get(args: List[RespType]): RespType = args match {
    case List(RespBulkString(Some(key))) =>
      Option(store.get(key)) match {
        case Some(value) => RespBulkString(Some(value))
        case None => RespNull()
      }
    case _ =>
      RespError("ERR wrong number of arguments for 'get' command")
  }
}
