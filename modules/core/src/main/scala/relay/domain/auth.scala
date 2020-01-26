package relay.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import java.util.UUID
import javax.crypto.Cipher
import scala.util.control.NoStackTrace
import dev.profunktor.auth.jwt._
import io.circe.Decoder

object auth {
  @newtype case class UserId(value: UUID)
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)

  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)
  @newtype case class DecryptCipher(value: Cipher)

  @newtype case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.value.toLowerCase)
  }

  @newtype case class PassworParam(value: NonEmptyString) {
    def toDomain: Password = Password(value.value)
  }

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @newtype case class CommonUser(value: User)
  @newtype case class AdminUser(value: User)

  case class User(
      id: UserId,
      name: UserName
  )

  case class CreateUser(
      username: UserNameParam,
      password: PassworParam
  )

  @newtype case class ClaimContent(uuid: UUID)

  object ClaimContent {
    implicit val jsonDecoder: Decoder[ClaimContent] = Decoder.forProduct1("uuid")(ClaimContent.apply)
  }

  case class UserNameInUse(username: UserName) extends NoStackTrace
  case class InvalidUserOrPassword(username: UserName) extends NoStackTrace
  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace

  case class LoginUser(
      username: UserNameParam,
      password: PassworParam
  )
}
