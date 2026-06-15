// PURPOSE: Unit test for the Datastar signals Tapir inputs — decoding lives in the codec layer.
// PURPOSE: A valid store reaches the handler already typed; a malformed one is a 400, handler unrun.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.*
import org.http4s.implicits.*
import sttp.tapir.ztapir.*
import utest.*
import works.iterative.scalatags.datastar.tapir.sse.SignalsInput
import zio.*
import zio.interop.catz.*

object SignalsInputTest extends TestSuite:

    private type F[A] = RIO[Any, A]

    private val runtime = Runtime.default

    private def run[A](z: F[A]): A =
        Unsafe.unsafe(implicit u => runtime.unsafe.run(z).getOrThrowFiberFailure())

    // Throwaway endpoints that echo the decoded store, so the test observes what the handler was
    // handed — proving the typed value, not a raw string, crosses the codec boundary.
    private val bodyEcho: ZServerEndpoint[Any, Any] =
        endpoint.post
            .in("body")
            .in(SignalsInput.body[Counter])
            .out(stringBody)
            .zServerLogic(counter => ZIO.succeed(counter.toString))

    private val queryEcho: ZServerEndpoint[Any, Any] =
        endpoint.get
            .in("query")
            .in(SignalsInput.query[ActiveSearch])
            .out(stringBody)
            .zServerLogic(search => ZIO.succeed(search.toString))

    private val app = HttpServer.routes(List(bodyEcho, queryEcho)).orNotFound

    private def call(request: Request[F]): (Response[F], String) =
        run(app.run(request).flatMap(resp => resp.as[String].map(resp -> _)))

    val tests = Tests:

        test("body decodes the JSON request body into the typed store"):
            val (response, body) = call(
                Request[F](Method.POST, uri"/body").withEntity("""{"count":5,"step":2}""")
            )
            assert(response.status.code == 200)
            assert(body == Counter(5, 2).toString)

        test("a body that does not fit the store is a 400, the handler never runs"):
            val (response, _) = call(
                Request[F](Method.POST, uri"/body").withEntity("""{"count":"nope"}""")
            )
            assert(response.status.code == 400)

        test("query decodes the datastar parameter into the typed store"):
            val (response, body) = call(
                Request[F](
                    Method.GET,
                    uri"/query".withQueryParam("datastar", """{"search":"go"}""")
                )
            )
            assert(response.status.code == 200)
            assert(body == ActiveSearch("go").toString)

        test("a datastar parameter that does not fit the store is a 400"):
            val (response, _) = call(
                Request[F](Method.GET, uri"/query".withQueryParam("datastar", "not json"))
            )
            assert(response.status.code == 400)

    end tests

end SignalsInputTest
