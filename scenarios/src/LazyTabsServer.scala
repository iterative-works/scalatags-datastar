// PURPOSE: Server logic for the lazy-tabs example — returns the whole widget for the chosen tab.
// PURPOSE: A 404-free demo: an out-of-range index falls back to the first tab rather than failing.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

/** The lazy-tabs example's handler: takes the tab index from the URL and streams the whole widget
  * with that tab selected, so the new `aria-selected` state and panel content arrive together.
  */
object LazyTabsServer:

    // snippet: lazy-tabs-server
    private val tabLogic: ZServerEndpoint[Any, ZioStreams] =
        LazyTabsEndpoints.tab.zServerLogic: index =>
            val selected = if Tabs.isDefined(index) then index else 0
            ZIO.succeed(datastarStream(ServerSentEvents.patchElements(LazyTabsView.tabs(selected))))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(tabLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just lazy tabs to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end LazyTabsServer
