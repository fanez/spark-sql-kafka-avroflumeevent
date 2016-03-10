package contrib.kafka.serializer

import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

import org.apache.avro.io.BinaryDecoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader

import org.apache.flume.Event
import org.apache.flume.event.EventBuilder
import org.apache.flume.source.avro.AvroFlumeEvent

import java.io.ByteArrayInputStream

class AvroFlumeEventDecoder(props: VerifiableProperties = null)
  extends Decoder[Event] {

  private val reader: SpecificDatumReader[AvroFlumeEvent] =
    new SpecificDatumReader[AvroFlumeEvent](classOf[AvroFlumeEvent])
  private var decoder: BinaryDecoder = null.asInstanceOf[BinaryDecoder]

  override def fromBytes(bytes: Array[Byte]): Event = {
    val inputStream = new ByteArrayInputStream(bytes)
    decoder = DecoderFactory.get.directBinaryDecoder(inputStream, decoder)
    val avroEvent: AvroFlumeEvent = reader.read(null, decoder)
    EventBuilder.withBody(
      avroEvent.getBody.array,
      toStringJavaMap(avroEvent.getHeaders))
  }

  def toStringJavaMap(
    charSeqMap: JMap[CharSequence, CharSequence]): JMap[String, String] = {

    import scala.collection.JavaConversions._
    for ((k: CharSequence, v: CharSequence) <- charSeqMap)
      yield (k.toString, v.toString)
  }
}
