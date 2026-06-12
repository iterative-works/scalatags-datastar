// PURPOSE: Server logic for the counter example — the SSE increment handler on the house stack.
// PURPOSE: Decodes the round-tripped store, advances it, and answers with a codec-built event.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.ZStream
import java.nio.charset.StandardCharsets.UTF_8
import works.iterative.scalatags.datastar.sse.ServerSentEvents

/** The counter example's action handler, wired to the house server stack.
  *
  * The increment handler is the round trip: it receives the request body already decoded into the
  * typed `Counter` store (by [[SignalsInput.body]]), advances it, and answers with a
  * `patch-signals` SSE event built by the codec — streamed out as `text/event-stream`. The codec
  * stays the single source of the wire bytes; tapir only carries the string it produced.
  */
object CounterServer:

    // snippet: counter-server
    private val incrementLogic: ZServerEndpoint[Any, ZioStreams] =
        CounterEndpoints.increment.zServerLogic: counter =>
            val event = ServerSentEvents.patchSignals(counter.incremented)
            ZIO.succeed(ZStream.fromChunk(Chunk.fromArray(event.getBytes(UTF_8))))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(incrementLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just the counter action to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end CounterServer
