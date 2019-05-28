package endpoints.http4s.client

import cats.MonadError
import cats.effect.Sync
import cats.implicits._
import endpoints.algebra
import endpoints.algebra.Documentation
import org.http4s.client.Client

import scala.language.higherKinds

case class Endpoints[F[_]: Sync](host: String, client: Client[F], syncF: Sync[F]) extends algebra.Endpoints with Methods[F] with Urls with Responses[F] with Requests[F] {

  override type Endpoint[A, B] = A => F[B]

  override def endpoint[A, B](
                               request: Request[A],
                               response: Response[B],
                               summary: Documentation,
                               description: Documentation,
                               tags: List[String]
                             ): Endpoint[A, B] =
    a => request(a).use(response).flatMap(MonadError[F, Throwable].fromEither)
}