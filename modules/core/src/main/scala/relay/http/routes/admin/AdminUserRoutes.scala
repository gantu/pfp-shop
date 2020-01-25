package relay.http.routes.admin

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import relay.algebras.Users
import relay.domain.auth._
import relay.http.decoder._
import relay.http.json._

final class AdminUserRoutes[F[_]: Sync](
    users: Users[F]
) extends Http4sDsl[F] {

  private[admin] val prefixPath = "/users"

  private val httpRoutes: AuthedRoutes[AdminUser, F] =
    AuthedRoutes.of {
      case ar @ POST -> Root as _ =>
        ar.req.decodeR[LoginUser] { u =>
          Created(users.create(u.username.toDomain, u.password.toDomain))
        }
    }

  def routes(authedMidleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authedMidleware(httpRoutes)
  )
}
