// PURPOSE: Integration test for the composed app — every example must route to its own page.
// PURPOSE: Guards against one example's endpoint shadowing another when all are mounted together.
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

        test("GET / serves the counter page"):
            val (response, body) = call(Request[F](Method.GET, uri"/"))
            assert(response.status.code == 200)
            assert(body.contains("""data-on:click="@post('/increment')""""))

        test("GET /search serves the live-search page, not the counter"):
            val (response, body) = call(Request[F](Method.GET, uri"/search"))
            assert(response.status.code == 200)
            assert(body.contains("""data-bind="query""""))
            assert(!body.contains("""@post('/increment')"""))

        test("GET /search/results serves the search action, not the counter page"):
            val request = Request[F](
                Method.GET,
                uri"/search/results".withQueryParam("datastar", """{"query":"go"}""")
            )
            val (response, body) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

        test("POST /increment serves the counter action"):
            val request = Request[F](Method.POST, uri"/increment")
                .withEntity("""{"count":0,"step":1}""")
            val (response, _) = call(request)
            assert(response.status.code == 200)
            assert(response.contentType.exists(_.mediaType == MediaType.`text/event-stream`))

    end tests

end ScenariosRoutesTest
