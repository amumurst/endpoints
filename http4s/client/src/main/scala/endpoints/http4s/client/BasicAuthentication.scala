package endpoints.http4s.client

import endpoints.algebra
import endpoints.algebra.BasicAuthentication.Credentials
import endpoints.algebra.Documentation
import org.http4s.BasicCredentials
import org.http4s.headers.Authorization

import scala.language.higherKinds


trait BasicAuthentication[F[_]] extends algebra.BasicAuthentication{ self: Endpoints[F] =>

  private[endpoints] lazy val basicAuthenticationHeader: RequestHeaders[Credentials] = (credentials, request) =>
    request.withHeaders(Authorization(BasicCredentials(credentials.username, credentials.password)))

  private[endpoints] def authenticated[A](inner: Response[A], docs: Documentation): Response[Option[A]] = {
    case res if res.status == org.http4s.Status.Forbidden => noneResponse[A]
    case res => syncF.map(inner(res))(_.right.map(Some(_)))
  }
}