package relay.ext
import io.estatico.newtype.Coercible
import skunk.Codec
import io.estatico.newtype.ops._

object skunkx {
  implicit class CodecOps[B](codec: Codec[B]) {
    def cimap[A: Coercible[B, *]]: Codec[A] =
      codec.imap(_.coerce[A])(_.repr.asInstanceOf[B])
  }
}
