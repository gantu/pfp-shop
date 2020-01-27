package relay.modules

import cats.effect._
import cats.implicits._
import dev.profunktor.auth.JwtAuthMiddleware
import dev.profunktor.auth.jwt.JwtToken
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.Router
import pdi.jwt._
import scala.concurrent.duration._
import relay.domain.auth._
import relay.http.routes.auth._
import relay.http.routes._
import relay.http.routes.admin.AdminBrandRoutes
//import relay.http.routes.secured._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
      algebras: Algebras[F],
      security: Security[F]
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        algebras,
        security
      )
    )
}

final class HttpApi[F[_]: Concurrent: Timer] private (
    algebras: Algebras[F],
    security: Security[F]
) {
  private val adminAuth: JwtToken => JwtClaim => F[Option[AdminUser]] =
    t => c => security.adminAuth.findUser(t)(c)
  private val usersAuth: JwtToken => JwtClaim => F[Option[CommonUser]] =
    t => c => security.usersAuth.findUser(t)(c)

  private val adminMiddleware = JwtAuthMiddleware[F, AdminUser](security.adminJwtAuth.value, adminAuth)
  private val usersMiddleware = JwtAuthMiddleware[F, CommonUser](security.userJwtAuth.value, usersAuth)

  // Auth routes
  private val loginRoutes  = new LoginRoutes[F](security.auth).routes
  private val logoutRoutes = new LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes   = new UserRoutes[F](security.auth).routes

  // Open routes
  //private val healthRoutes   = new HealthRoutes[F](algebras.healthCheck).routes
  private val brandRoutes    = new BrandRoutes[F](algebras.brands).routes
  private val categoryRoutes = new CategoryRoutes[F](algebras.categories).routes
  //private val itemRoutes     = new ItemRoutes[F](algebras.items).routes

  // Secured routes
  //private val cartRoutes     = new CartRoutes[F](algebras.cart).routes(usersMiddleware)
  //private val checkoutRoutes = new CheckoutRoutes[F](programs.checkout).routes(usersMiddleware)
  //private val orderRoutes    = new OrderRoutes[F](algebras.orders).routes(usersMiddleware)

  // Admin routes
  // private val adminUserRoutes = new AdminUserRoutes[F](security.users).routes(adminMiddleware)
  private val adminBrandRoutes = new AdminBrandRoutes[F](algebras.brands).routes(adminMiddleware)
  //private val adminCategoryRoutes = new AdminCategoryRoutes[F](algebras.categories).routes(adminMiddleware)
  //private val adminItemRoutes     = new AdminItemRoutes[F](algebras.items).routes(adminMiddleware)

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    loginRoutes <+> userRoutes <+> logoutRoutes <+> brandRoutes <+> categoryRoutes

  private val adminRoutes: HttpRoutes[F] = adminBrandRoutes
  //adminItemRoutes <+> adminBrandRoutes <+> adminCategoryRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes,
    version.v1 + "/admin" -> adminRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(true, true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(true, true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
