// PURPOSE: Server logic for the infinite-scroll example — appends a page and re-arms the sentinel.
// PURPOSE: Three events: append the rows, patch a fresh sentinel, advance the offset signal.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

/** The infinite-scroll example's handler: reads the offset from the store, appends that page's
  * rows, patches a fresh sentinel (re-armed while rows remain, inert at the end), and advances the
  * offset.
  */
object InfiniteScrollServer:

    // snippet: infinite-scroll-server
    private val moreLogic: ZServerEndpoint[Any, ZioStreams] =
        InfiniteScrollEndpoints.more.zServerLogic: store =>
            val offset = store.offset
            val nextOffset = offset + Agents.pageSize
            val appended = ServerSentEvents.patchElements(
                InfiniteScrollView.rows(Agents.page(offset)),
                selector = Some("#agents"),
                mode = ElementPatchMode.Append
            )
            val rearm = ServerSentEvents.patchElements(InfiniteScrollView.sentinel(nextOffset))
            val advanced = ServerSentEvents.patchSignals(InfiniteScroll(nextOffset))
            ZIO.succeed(datastarStream(appended, rearm, advanced))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(moreLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just infinite-scroll to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end InfiniteScrollServer
