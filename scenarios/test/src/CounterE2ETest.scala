// PURPOSE: End-to-end test — boots the real Blaze server and drives it with a real HTTP client.
// PURPOSE: Proves the full round trip: typed action, readSignals decode, patch-signals response.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import sttp.client3.*
import works.iterative.scalatags.datastar.sse.ServerSentEvents

object CounterE2ETest extends TestSuite:

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
        run(ZIO.scoped(CounterServer.serve(port).flatMap { _ =>
            ZIO.attemptBlocking {
                val backend = HttpClientSyncBackend()
                try f(port, backend)
                finally backend.close()
            }
        }))

    val tests = Tests:

        test("POST /increment decodes the store, increments, and streams a patch-signals event"):
            withServer: (port, backend) =>
                val response = basicRequest
                    .post(uri"http://localhost:$port/increment")
                    .header("Content-Type", "application/json")
                    .body("""{"count":5,"step":1}""")
                    .response(asStringAlways)
                    .send(backend)
                assert(response.code.code == 200)
                assert(response.contentType.exists(_.contains("text/event-stream")))
                // The wire bytes are exactly what the conformance-tested codec produces.
                assert(response.body == ServerSentEvents.patchSignals(Counter(6, 1)))

        test("POST /increment honours the step when advancing the count"):
            withServer: (port, backend) =>
                val response = basicRequest
                    .post(uri"http://localhost:$port/increment")
                    .header("Content-Type", "application/json")
                    .body("""{"count":10,"step":5}""")
                    .response(asStringAlways)
                    .send(backend)
                assert(response.body.contains("""data: signals {"count":15,"step":5}"""))

        test("POST /increment rejects a malformed signal store with 400"):
            withServer: (port, backend) =>
                val response = basicRequest
                    .post(uri"http://localhost:$port/increment")
                    .header("Content-Type", "application/json")
                    .body("""{"count":"not a number"}""")
                    .response(asStringAlways)
                    .send(backend)
                assert(response.code.code == 400)

    end tests

end CounterE2ETest
