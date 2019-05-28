package endpoints.http4s.client

import endpoints.algebra
import org.http4s.{Request, Method => Http4sMethod}

import scala.language.higherKinds


trait Methods[F[_]] extends algebra.Methods{
  /** HTTP Method */

  override type Method = Request[F] => Request[F]

  private def setMethod(m: Http4sMethod): Method = _.withMethod(m)

  override def Get: Method      = setMethod(Http4sMethod.GET)
  override def Post: Method     = setMethod(Http4sMethod.POST)
  override def Put: Method      = setMethod(Http4sMethod.PUT)
  override def Delete: Method   = setMethod(Http4sMethod.DELETE)
  override def Patch: Method    = setMethod(Http4sMethod.PATCH)
  override def Options: Method  = setMethod(Http4sMethod.OPTIONS)
}