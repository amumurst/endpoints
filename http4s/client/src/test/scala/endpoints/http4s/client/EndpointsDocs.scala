package endpoints.http4s.client

import cats.effect.IO
import endpoints.algebra

trait EndpointsDocs extends Endpoints[IO] with algebra.EndpointsDocs {

  //#invocation
  val stringIO: IO[String] = someResource(42)
  val string: String = stringIO.unsafeRunSync()
  //#invocation

}
