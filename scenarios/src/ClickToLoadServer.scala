// PURPOSE: Server logic for the click-to-load example — appends the next page, advances the offset.
// PURPOSE: Three events: append the rows, re-render the button, patch the offset signal forward.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import works.iterative.scalatags.datastar.tapir.sse.*

/** The click-to-load example's handler: reads the offset from the store, appends that page's rows
  * into the table, re-renders the Load-more control, and patches the offset forward so the next
  * click continues where this one left off.
  */
object ClickToLoadServer:

    // snippet: click-to-load-server
    private val moreLogic: ZServerEndpoint[Any, ZioStreams] =
        ClickToLoadEndpoints.more.zServerLogic: store =>
            val offset = store.offset
            val nextOffset = offset + Agents.pageSize
            val appended = ServerSentEvents.patchElements(
                ClickToLoadView.rows(Agents.page(offset)),
                selector = Some("#agents"),
                mode = ElementPatchMode.Append
            )
            val control = ServerSentEvents.patchElements(ClickToLoadView.loadMore(nextOffset))
            val advanced = ServerSentEvents.patchSignals(ClickToLoad(nextOffset))
            ZIO.succeed(datastarStream(appended, control, advanced))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(moreLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just click-to-load to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end ClickToLoadServer
