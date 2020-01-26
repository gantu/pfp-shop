package relay.http.routes

import cats.effect.Sync
import relay.algebras.Categories
import relay.http.json._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.Router

final class CategoryRoutes[F[_]: Sync](categories: Categories[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/categories"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok(categories.findAll)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
