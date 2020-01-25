package relay.http.routes.auth

import cats.effect.Sync
import org.http4s._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import relay.domain.auth._
import relay.http.decoder._
import relay.http.json._
import relay.algebras.Auth

final class LoginRoutes[F[_]: Sync](
    auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .handleErrorWith {
            case InvalidUserOrPassword(_) => Forbidden()
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
