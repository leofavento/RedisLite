package store

import protocol.{RespArray, RespBulkString, RespError, RespInteger, RespNull, RespString, RespType}

import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ArrayBuffer

class HashStore {
  private val store = ConcurrentHashMap[String, ConcurrentHashMap[String, String]]()

  def hset(args: List[RespType]): RespType = {
    if (args.length < 3 || (args.length - 1) % 2 != 0) {
      return RespError("ERR wrong number of arguments for 'hset' command")
    }
    val key = args.headOption.collect { case RespBulkString(Some(key)) => key }
    key match {
      case Some(key) =>
        val fieldValuePairs = args.tail.grouped(2).collect {
          case List(RespBulkString(Some(field)), RespBulkString(Some(value))) => (field, value)
        }.toList
      case None => RespError("ERR invalid key format")
    }
    args.headOption.flatMap {
      case RespBulkString(Some(key)) => Some(key)
      case _ => None
    } match {
      case Some(key) =>
        val fieldValues = args.tail // Remove the key from args
        val fieldValuePairs = fieldValues.grouped(2).collect {
          case List(RespBulkString(Some(field)), RespBulkString(Some(value))) => (field, value)
        }.toList
        if (fieldValuePairs.length != (args.length - 1) / 2) {
          return RespError("ERR wrong number of arguments for 'hset' command")
        }
        val existingMap = Option(store.get(key)).getOrElse {
          val newMap = new java.util.concurrent.ConcurrentHashMap[String, String]()
          store.put(key, newMap)
          newMap
        }
        val counter = fieldValuePairs.count { case (field, value) =>
          existingMap.put(field, value) != null
        }
        RespInteger(counter)
      case None => RespError("ERR invalid key format")
    }
  }

  def hget(args: List[RespType]): RespType = args match {
    case List(RespBulkString(Some(key)), RespBulkString(Some(field))) =>
      Option(store.get(key)) match {
        case Some(hash: ConcurrentHashMap[String, String]) =>
          Option(hash.get(field)) match {
            case Some(value) => RespBulkString(Some(value))
            case None => RespNull()
          }
        case None => RespNull()
      }
    case _ =>
      RespError("ERR wrong number of arguments for 'hget' command")
  }

  def hgetall(args: List[RespType]): RespType = args match {
    case List(RespBulkString(Some(key))) =>
      Option(store.get(key)) match {
        case Some(hash: ConcurrentHashMap[String, String]) =>
          val content = ArrayBuffer[RespType]()
          hash.entrySet().iterator().forEachRemaining { entry =>
            val key = entry.getKey
            val value = entry.getValue

            content += RespBulkString(Some(key))
            content += RespBulkString(Some(value))
          }
          RespArray(content.toList)
        case None => RespArray(List())
      }
    case _ =>
      RespError("ERR wrong number of arguments for 'hgetall' command")
  }
}
