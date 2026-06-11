// PURPOSE: Integration test — drives the assembled live-search routes in-process, without a socket.
// PURPOSE: Proves the GET action's `datastar` query param decodes and patch-elements answers it.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import zio.interop.catz.*
import org.http4s.*
import org.http4s.implicits.*
import works.iterative.scalatags.datastar.sse.ServerSentEvents

object SearchRoutesTest extends TestSuite:

    private type F[A] = RIO[Any, A]

    private val runtime = Runtime.default

    private def run[A](z: F[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    private val app = SearchServer.routes.orNotFound

    /** Runs one request through the routes and reads the full (possibly streamed) body. */
    private def call(request: Request[F]): (Response[F], String) =
        run(app.run(request).flatMap(resp => resp.as[String].map(resp -> _)))

    val tests = Tests:

        test("GET /search/results streams the patch-elements event for the filtered list"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", """{"query":"sca"}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))
            assert(body == ServerSentEvents.patchElements(SearchView.results(Languages.matching("sca"))))

    end tests

end SearchRoutesTest
