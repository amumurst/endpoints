package endpoints.http4s.client

import cats.effect.Sync
import cats.implicits._
import endpoints.algebra
import endpoints.algebra.Documentation
import org.http4s
import org.http4s.{EntityDecoder, Status, Response => hReponse}

import scala.language.higherKinds

trait Responses[F[_]] extends algebra.Responses{ self: Endpoints[F] =>
  type Responded[A] = F[Either[Throwable, A]]

  override type Response[A] = hReponse[F] => Responded[A]

  def noneResponse[A]: Responded[Option[A]] = Either.right[Throwable, Option[A]](Option.empty).pure[F]

  private implicit lazy val syncFImplicit: Sync[F] = self.syncF

  private def errorResponse[A](r: hReponse[F]): F[Either[Throwable, A]] = Either.left[Throwable, A](new Throwable(s"Unexpected status code: ${r.status.code}")).pure[F]

  override def emptyResponse(docs: Documentation): Response[Unit] =  {
    case r if r.status.isSuccess => Either.right[Throwable, Unit](()).pure[F]
    case r => errorResponse(r)
  }

  def decodeString(response: http4s.Message[F]): F[String] = EntityDecoder.decodeString(response)

  override def textResponse(docs: Documentation): Response[String] = {
    case r if r.status.isSuccess => self.decodeString(r).map(s => Right(s))
    case r => errorResponse(r)
  }

  override def wheneverFound[A](response: Response[A], notFoundDocs: Documentation): Response[Option[A]] = {
    case r if r.status == Status.NotFound => noneResponse[A]
    case r => syncF.map(response(r))(_.map(Some(_)))
  }
}