package contrib.kafka

package object serializer {

  type JList[T] = java.util.List[T]

  type JMap[T, E] = java.util.Map[T, E]
}
