package relay.modules
import cats.Parallel
import cats.effect._
import cats.implicits._
//import dev.profunktor.redis4cats.algebra.RedisCommands
import relay.algebras._

import skunk._

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
      sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] =
    for {
      brands <- LiveBrands.make[F](sessionPool)
      categories <- LiveCategories.make[F](sessionPool)
    } yield new Algebras[F](brands, categories)
}

final class Algebras[F[_]] private (
    val brands: Brands[F],
    val categories: Categories[F]
) {}
