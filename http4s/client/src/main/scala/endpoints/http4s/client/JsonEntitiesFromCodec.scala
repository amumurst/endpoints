package endpoints.http4s.client

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import endpoints.algebra.{Codec, Documentation}
import org.http4s.headers.`Content-Type`
import org.http4s.{EntityDecoder, InvalidMessageBodyFailure, MediaType}

import scala.language.higherKinds

/**
  * Interpreter for [[endpoints.algebra.JsonEntitiesFromCodec]] that encodes JSON request
  * @group interpreters
  */
trait JsonEntitiesFromCodec[F[_]] extends endpoints.algebra.JsonEntitiesFromCodec { self: Endpoints[F] =>

  private implicit lazy val applicativeFImplicit: Applicative[F] = self.syncF

  private implicit def entityDecoderForCodec[A](implicit codec: Codec[String, A]): EntityDecoder[F, A] =
    EntityDecoder
      .decodeBy[F, A](org.http4s.MediaType.application.json){message =>
        EitherT(
          self.decodeString(message).map(s =>
            codec.decode(s) match {
              case Left(value) => Left(InvalidMessageBodyFailure("error parsing entity", Some(value)))
              case Right(value) => Right(value)
            }
          )
        )
    }

  def jsonRequest[A](docs: Documentation)(implicit codec: Codec[String, A]): RequestEntity[A] = (a, req) => {
    req.withEntity(codec.encode(a)).withContentType(`Content-Type`( MediaType.application.json))
  }


  def jsonResponse[A](docs: Documentation)(implicit codec: Codec[String, A]): Response[A] = response => {
    response.attemptAs[A].leftMap(_.getCause()).value
  }
}
