package relay.http

import relay.domain.auth._
import relay.domain.brand._

import cats.effect.Sync
import dev.profunktor.auth.jwt.JwtToken
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.{ EntityDecoder, EntityEncoder }
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import relay.domain.category.Category

object json {
  implicit def jsonDecoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]
  implicit def jsonEncoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  implicit val brandParamDecoder: Decoder[BrandParam] =
    Decoder.forProduct1("name")(BrandParam.apply)

  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] = Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] = KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
  implicit val userEncoder: Encoder[User] = deriveEncoder[User]

  implicit val createUserDecoder: Decoder[CreateUser] = deriveDecoder[CreateUser]
  implicit val loginUserDecoder: Decoder[LoginUser]   = deriveDecoder[LoginUser]

  implicit val brandDecoder: Decoder[Brand] = deriveDecoder[Brand]
  implicit val brandEncoder: Encoder[Brand] = deriveEncoder[Brand]

  implicit val categoryDecoder: Decoder[Category] = deriveDecoder[Category]
  implicit val categoryEncoder: Encoder[Category] = deriveEncoder[Category]

}
