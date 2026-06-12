// PURPOSE: Integration test for the composed app — the gallery pages and every example's action.
// PURPOSE: Guards against one endpoint shadowing another, and proves source is read through a route.
package works.iterative.scalatags.datastar.scenarios

import utest.*
import zio.*
import zio.interop.catz.*
import org.http4s.*
import org.http4s.implicits.*

object ScenariosRoutesTest extends TestSuite:

    private type F[A] = RIO[Any, A]

    private val runtime = Runtime.default

    private def run[A](z: F[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    private val app = Scenarios.routes.orNotFound

    private def call(request: Request[F]): (Response[F], String) =
        run(app.run(request).flatMap(resp => resp.as[String].map(resp -> _)))

    val tests = Tests:

        test("GET / serves the gallery home listing every demo"):
            val (response, body) = call(Request[F](Method.GET, uri"/"))
            assert(response.status.code == 200)
            assert(body.contains("""href="/examples/counter""""))
            assert(body.contains("""href="/examples/search""""))

        test("GET /examples/counter serves the counter demo beside its source"):
            val (response, body) = call(Request[F](Method.GET, uri"/examples/counter"))
            assert(response.status.code == 200)
            // the live widget...
            assert(body.contains("""data-on:click="@post('/increment')""""))
            // ...and its source, read from the classpath and marked for the highlighter
            assert(body.contains("""class="language-scala""""))
            assert(body.contains("final case class Counter"))

        test("GET /examples/search serves the live-search demo, not the counter"):
            val (response, body) = call(Request[F](Method.GET, uri"/examples/search"))
            assert(response.status.code == 200)
            assert(body.contains("""data-bind="query""""))
            assert(!body.contains("""@post('/increment')"""))

        test("GET /examples/{unknown} is a 404"):
            val (response, _) = call(Request[F](Method.GET, uri"/examples/nope"))
            assert(response.status.code == 404)

        test("POST /increment serves the counter action"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":0,"step":1}""")
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

        test("GET /search/results serves the search action"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", """{"query":"go"}""")
            )
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

        test("POST /increment with a body that does not fit the store is a 400"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":"nope"}""")
            val (response, _) = call(request)
            assert(response.status.code == 400)

        test("GET /search/results with a datastar param that does not fit the store is a 400"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", "not json")
            )
            val (response, _) = call(request)
            assert(response.status.code == 400)

    end tests

end ScenariosRoutesTest
