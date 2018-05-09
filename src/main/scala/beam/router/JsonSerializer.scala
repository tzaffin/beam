package beam.router

import akka.serialization.Serializer
import beam.router.Modes.BeamMode
import io.circe.syntax._

class JsonSerializer extends Serializer {

  import beam.router.Modes.BeamMode._

  override val identifier: Int = 12345
  override val includeManifest: Boolean = false

  override def toBinary(o: AnyRef): Array[Byte] =
    o.asInstanceOf[BeamMode].asJson.toString().getBytes

  override def fromBinary(bytes: Array[Byte],
                          manifest: Option[Class[_]]): AnyRef =
    new String(bytes).asJson.as[BeamMode]
}
