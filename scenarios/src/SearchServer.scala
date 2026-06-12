// PURPOSE: Server logic for the live-search example — the SSE search handler on the house stack.
// PURPOSE: Decodes the `datastar` query param, filters the catalogue, patches the same results Frag.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.ZStream
import java.nio.charset.StandardCharsets.UTF_8
import works.iterative.scalatags.datastar.sse.ServerSentEvents

/** The live-search example's action handler, wired to the house server stack.
  *
  * The search handler receives the `datastar` query parameter already decoded into the typed
  * `Search` store (by [[SignalsInput.query]]), filters the catalogue, and answers with a
  * `patch-elements` SSE event built from the very `SearchView.results` fragment the page first
  * rendered — so the patched list and the initial list are produced by one template. The codec
  * stays the source of the wire bytes.
  */
object SearchServer:

    // snippet: search-server
    private val searchLogic: ZServerEndpoint[Any, ZioStreams] =
        SearchEndpoints.search.zServerLogic: search =>
            val event = ServerSentEvents.patchElements(
                SearchView.results(Languages.matching(search.query))
            )
            ZIO.succeed(ZStream.fromChunk(Chunk.fromArray(event.getBytes(UTF_8))))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(searchLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just live search to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end SearchServer
