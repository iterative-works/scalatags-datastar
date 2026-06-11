// PURPOSE: Integration test — drives the assembled http4s routes in-process, without a socket.
// PURPOSE: Isolates the Tapir→http4s interpretation and the streaming SSE body from the network.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import zio.interop.catz.*
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.headers.`Content-Type`
import works.iterative.scalatags.datastar.sse.ServerSentEvents

object CounterRoutesTest extends TestSuite:

    private type F[A] = RIO[Any, A]

    private val runtime = Runtime.default

    private def run[A](z: F[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    private val app = CounterServer.routes.orNotFound

    /** Runs one request through the routes and reads the full (possibly streamed) body. */
    private def call(request: Request[F]): (Response[F], String) =
        run(app.run(request).flatMap(resp => resp.as[String].map(resp -> _)))

    val tests = Tests:

        test("POST /increment streams the patch-signals event for the advanced store"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":5,"step":1}""")
                .withContentType(`Content-Type`(MediaType.application.json))
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))
            assert(body == ServerSentEvents.patchSignals(Counter(6, 1)))

    end tests

end CounterRoutesTest
