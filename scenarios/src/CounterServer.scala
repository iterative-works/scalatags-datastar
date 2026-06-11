// PURPOSE: Server logic for the counter example — page rendering and the SSE increment handler.
// PURPOSE: Wires the Tapir endpoints to http4s/Blaze under ZIO, keeping the SSE codec authoritative.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import zio.*
import zio.stream.ZStream
import zio.interop.catz.*
import java.nio.charset.StandardCharsets.UTF_8
import works.iterative.scalatags.datastar.sse.{ServerSentEvents, readSignals}

/** The counter example, wired to the house server stack.
  *
  * The page handler just renders the template. The increment handler is the round trip: it decodes
  * the request body into the typed `Counter` store with `readSignals`, advances it, and answers
  * with a `patch-signals` SSE event built by the codec — streamed out as `text/event-stream`. The
  * codec stays the single source of the wire bytes; tapir only carries the string it produced.
  */
object CounterServer:

    private val pageLogic: ZServerEndpoint[Any, Any] =
        CounterEndpoints.page.zServerLogic(_ => ZIO.succeed(CounterView.page))

    private val incrementLogic: ZServerEndpoint[Any, ZioStreams] =
        CounterEndpoints.increment.zServerLogic: body =>
            readSignals[Counter](body) match
                case Right(counter) =>
                    val event = ServerSentEvents.patchSignals(counter.incremented)
                    ZIO.succeed(ZStream.fromChunk(Chunk.fromArray(event.getBytes(UTF_8))))
                case Left(error) =>
                    ZIO.fail(s"Could not read signals: $error")

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(pageLogic, incrementLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        ZHttp4sServerInterpreter[Any]().from(serverEndpoints).toRoutes

    /** Binds a Blaze server to `host`/`port` (use `0` for an ephemeral port), scoped so it shuts
      * down when the scope closes.
      */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        for
            executor <- ZIO.executor
            server <- BlazeServerBuilder[[A] =>> RIO[Any, A]]
                .withExecutionContext(executor.asExecutionContext)
                .bindHttp(port, host)
                .withHttpApp(routes.orNotFound)
                .resource
                .toScopedZIO
        yield server

end CounterServer
