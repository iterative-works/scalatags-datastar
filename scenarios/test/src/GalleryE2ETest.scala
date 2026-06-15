// PURPOSE: End-to-end test — boots real Blaze with the whole gallery and drives it over a socket.
// PURPOSE: Proves the home index, an example page with classpath-read source, and a 404 for unknown.
package works.iterative.scalatags.datastar.scenarios

import sttp.client3.*
import sttp.model.Uri
import utest.*
import zio.*

object GalleryE2ETest extends TestSuite:

    private val runtime = Runtime.default

    private def run[A](z: ZIO[Any, Throwable, A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    /** A free local port; the tiny window before Blaze binds it is harmless for a test. */
    private def freePort(): Int =
        val socket = new java.net.ServerSocket(0)
        try socket.getLocalPort
        finally socket.close()

    /** Boots the whole composed app on a fresh port, runs `f` against it, then stops it. */
    private def withServer[A](f: (Int, SttpBackend[Identity, Any]) => A): A =
        val port = freePort()
        run(ZIO.scoped(HttpServer.serve(Scenarios.endpoints, port).flatMap { _ =>
            ZIO.attemptBlocking {
                val backend = HttpClientSyncBackend()
                try f(port, backend)
                finally backend.close()
            }
        }))
    end withServer

    private def fetch(backend: SttpBackend[Identity, Any], uri: Uri) =
        basicRequest.get(uri).response(asStringAlways).send(backend)

    val tests = Tests:

        test("GET / serves the gallery home as HTML linking every demo"):
            withServer: (port, backend) =>
                val response = fetch(backend, uri"http://localhost:$port/")
                assert(response.code.code == 200)
                assert(response.contentType.exists(_.contains("text/html")))
                assert(response.body.contains("""href="/examples/counter""""))
                assert(response.body.contains("""href="/examples/active-search""""))

        test("GET /examples/counter serves the live demo and its highlighted source"):
            withServer: (port, backend) =>
                val response = fetch(backend, uri"http://localhost:$port/examples/counter")
                assert(response.code.code == 200)
                assert(response.body.contains("""data-on:click="@post('/increment')""""))
                assert(response.body.contains("""class="language-scala""""))
                // The source is loaded from the classpath, so the real file's text is on the page.
                assert(response.body.contains("final case class Counter"))
                assert(
                    response.body.contains(s"highlightjs/cdn-release@${Gallery.highlightVersion}")
                )

        test("GET /examples/{unknown} returns 404"):
            withServer: (port, backend) =>
                val response = fetch(backend, uri"http://localhost:$port/examples/nope")
                assert(response.code.code == 404)

    end tests

end GalleryE2ETest
