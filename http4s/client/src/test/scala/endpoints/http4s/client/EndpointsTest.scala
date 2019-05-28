package endpoints.http4s.client

import cats.effect._
import endpoints.algebra.circe.JsonFromCirceCodecTestApi
import endpoints.algebra.client.{BasicAuthTestSuite, EndpointsTestSuite, JsonFromCodecTestSuite}
import endpoints.algebra.{BasicAuthTestApi, EndpointsTestApi}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds


class TestClient[R[_]: Sync](address: String, client: Client[R])
  extends Endpoints(address, client, Sync[R])
    with JsonEntitiesFromCodec[R]
    with BasicAuthentication[R]
    with EndpointsTestApi
    with BasicAuthTestApi
    with JsonFromCirceCodecTestApi


class EndpointsTest
  extends EndpointsTestSuite[TestClient[IO]]
    with JsonFromCodecTestSuite[TestClient[IO]]
    with BasicAuthTestSuite[TestClient[IO]] {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val t: Timer[IO] = IO.timer(ExecutionContext.global)

  lazy val (underlayingClient,_) = BlazeClientBuilder[IO](ExecutionContext.global).resource.allocated.unsafeRunSync

  override lazy val client: TestClient[IO] = new TestClient[IO](s"http://localhost:$wiremockPort", underlayingClient)

  override def call[Req, Resp](endpoint: Req => IO[Resp], args: Req): Future[Resp] = endpoint(args).unsafeToFuture()

  override def encodeUrl[A](url: client.Url[A])(a: A): String = url.encode(a)

  clientTestSuite()
  basicAuthSuite()
  jsonFromCodecTestSuite()
}


