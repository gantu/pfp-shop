
package relay.http.routes.admin

import cats.Defer
import org.http4s.circe.JsonDecoder
import relay.effects.`package`.MonadThrow
import relay.algebras.Brands
import org.http4s.dsl.Http4sDsl
import org.http4s.AuthedRoutes
import relay.domain.auth.AdminUser
import relay.domain.brand.BrandParam
import org.http4s.server.`package`.AuthMiddleware
import org.http4s.HttpRoutes
import org.http4s.server.Router

final class AdminBrandRoutes[F[_]: Defer: JsonDecoder: MonadThrow](
    brands: Brands[F]
) extends Http4sDsl[F] {

    private[admin] val prefixPath = "/brands"

    private val httpRoutes: AuthedRoutes[AdminUser, F] = 
        AuthedRoutes.of {
            case ar @ POST -> Root as _ => 
                ar.req.decodeR[BrandParam] { bp => 
                    Created(brands.create(bp.toDomain))
                }
        }

    def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
        prefixPath -> authMiddleware(httpRoutes)
    )
}