// PURPOSE: End-to-end test — boots real Blaze and drives live search with a real HTTP client.
// PURPOSE: Proves the GET round trip: `datastar` query param decode, filter, patch-elements response.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import sttp.client3.*
import works.iterative.scalatags.datastar.sse.ServerSentEvents

object SearchE2ETest extends TestSuite:

    private val runtime = Runtime.default

    private def run[A](z: ZIO[Any, Throwable, A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    /** A free local port; the tiny window before Blaze binds it is harmless for a test. */
    private def freePort(): Int =
        val socket = new java.net.ServerSocket(0)
        try socket.getLocalPort
        finally socket.close()

    /** Boots the server on a fresh port, runs `f` against it over a real socket, then stops it. */
    private def withServer[A](f: (Int, SttpBackend[Identity, Any]) => A): A =
        val port = freePort()
        run(ZIO.scoped(SearchServer.serve(port).flatMap { _ =>
            ZIO.attemptBlocking {
                val backend = HttpClientSyncBackend()
                try f(port, backend)
                finally backend.close()
            }
        }))

    /** Issues the search action the way the Datastar client does: signals in the `datastar` param. */
    private def search(port: Int, backend: SttpBackend[Identity, Any], store: String) =
        basicRequest
            .get(uri"http://localhost:$port/search/results".addParam("datastar", store))
            .response(asStringAlways)
            .send(backend)

    val tests = Tests:

        test("GET /search/results decodes the store, filters, and streams a patch-elements event"):
            withServer: (port, backend) =>
                val response = search(port, backend, """{"query":"sca"}""")
                assert(response.code.code == 200)
                assert(response.contentType.exists(_.contains("text/event-stream")))
                // The wire bytes are exactly what the conformance-tested codec produces.
                assert(response.body == ServerSentEvents.patchElements(SearchView.results(Seq("Scala"))))

        test("GET /search/results returns the whole catalogue for an empty query"):
            withServer: (port, backend) =>
                val response = search(port, backend, """{"query":""}""")
                assert(response.body == ServerSentEvents.patchElements(SearchView.results(Languages.all)))

        test("GET /search/results reports no matches when nothing fits"):
            withServer: (port, backend) =>
                val response = search(port, backend, """{"query":"zzz"}""")
                assert(response.body.contains("No matches"))

        test("GET /search/results rejects a malformed signal store with 400"):
            withServer: (port, backend) =>
                val response = search(port, backend, """{"query":42}""")
                assert(response.code.code == 400)

    end tests

end SearchE2ETest
