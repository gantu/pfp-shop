package relay

import cats.effect.Bracket
import cats.mtl.ApplicativeAsk
import cats.MonadError
import cats.ApplicativeError

package object effects {

  type BracketThrow[F[_]] = Bracket[F, Throwable]

  object BracketThrow {
    def apply[F[_]](implicit ev: Bracket[F, Throwable]): BracketThrow[F] = ev
  }

  type AppThrow[F[_]] = ApplicativeError[F, Throwable]

  object AppThrow {
    def apply[F[_]](implicit ev: ApplicativeError[F, Throwable]): AppThrow[F] = ev
  }

  type MonadThrow[F[_]] = MonadError[F, Throwable]

  object MonadThrow {
    def apply[F[_]](implicit ev: MonadError[F, Throwable]): MonadThrow[F] = ev
  }

  def ask[F[_], A](implicit ev: ApplicativeAsk[F, A]): F[A] = ev.ask
}
