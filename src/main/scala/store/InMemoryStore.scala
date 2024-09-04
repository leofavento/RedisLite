package store

import protocol.RespType

class InMemoryStore {
  val stringStore = StringStore()
  val hashStore = HashStore()

  def set(args: List[RespType]): RespType = stringStore.set(args)

  def get(args: List[RespType]): RespType = stringStore.get(args)

  def hset(args: List[RespType]): RespType = hashStore.hset(args)

  def hget(args: List[RespType]): RespType = hashStore.hget(args)

  def hgetall(args: List[RespType]): RespType = hashStore.hgetall(args)
}
