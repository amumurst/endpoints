package endpoints.http4s.client

import cats.effect.Resource
import endpoints.algebra.Documentation
import endpoints.{InvariantFunctor, Semigroupal, Tupler, algebra}
import org.http4s.{Header, Uri}

import scala.language.higherKinds

trait Requests[F[_]] extends algebra.Requests with Methods[F] with Urls {self: Endpoints[F] =>

  type Http4sRequest = org.http4s.Request[F]

  override type RequestHeaders[A] = (A, Http4sRequest) => Http4sRequest

  override def emptyHeaders: RequestHeaders[Unit]= (_, r) => r

  override def header(name: String, docs: Documentation = None): RequestHeaders[String] = (h, r) => {
    r.withHeaders(r.headers.toList :+ Header(name, h): _*)
  }

  override def optHeader(name: String, docs: Documentation = None): RequestHeaders[Option[String]] = {
    case (None, r) => r
    case (Some(h), r) => header(name,  docs)(h, r)
  }

  override implicit def reqHeadersSemigroupal: Semigroupal[RequestHeaders]= new Semigroupal[RequestHeaders] {
    override def product[A, B](
                                fa: (A, Http4sRequest) => Http4sRequest,
                                fb: (B, Http4sRequest) => Http4sRequest)
                              (implicit tupler: Tupler[A, B]): (tupler.Out, Http4sRequest) => Http4sRequest =
      (ab, request) => {
        val (a, b) = tupler.unapply(ab)

        fb(b, fa(a, request))
      }
  }
  override implicit def reqHeadersInvFunctor: InvariantFunctor[RequestHeaders] = new InvariantFunctor[RequestHeaders] {
    override def xmap[From, To](f: (From, Http4sRequest) => Http4sRequest, map: From => To, contramap: To => From): (To, Http4sRequest) => Http4sRequest =
      (to, request) => f(contramap(to), request)
  }

  override type Request[A] = A => Resource[F, org.http4s.Response[F]]

  override type RequestEntity[A] = (A, Http4sRequest) => Http4sRequest

  implicit def reqEntityInvFunctor: InvariantFunctor[RequestEntity] = new InvariantFunctor[RequestEntity] {
    override def xmap[From, To](f: (From, Http4sRequest) => Http4sRequest, map: From => To, contramap: To => From): (To, Http4sRequest) => Http4sRequest =
      (to, req) => f(contramap(to), req)
  }


  lazy val emptyRequest: RequestEntity[Unit] = {
    case (_, req) => req.withEmptyBody
  }

  def textRequest(docs: Option[String]): RequestEntity[String] = {
    case (bodyValue, request) => request.withEntity(bodyValue)
  }

  override def request[A, B, C, AB, Out](
                                 method: Method, url: Url[A],
                                 entity: RequestEntity[B], headers: RequestHeaders[C]
                               )(implicit tuplerAB: Tupler.Aux[A, B, AB], tuplerABC: Tupler.Aux[AB, C, Out]): Request[Out] = (abc: Out) => {
    val (ab, c) = tuplerABC.unapply(abc)
    val (a, b) = tuplerAB.unapply(ab)

    val uri: Uri = org.http4s.Uri.unsafeFromString(s"$host${url.encode(a)}")

    client.run(entity(b, headers(c, method(org.http4s.Request[F](uri = uri)))))
  }
}