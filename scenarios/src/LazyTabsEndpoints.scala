// PURPOSE: Tapir endpoints for the lazy-tabs example — a typed Int path route and its SSE form.
// PURPOSE: The tab index is an explicit URL parameter (the other channel), never the signal store.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The lazy-tabs example's route.
  *
  * Unlike the counter and search, this route has a real typed input — the tab index as an `Int`
  * path parameter — which `endpoint.action(index)` reverse-routes into the per-tab action URL.
  * There is no signal store, so the explicit-parameter channel is the only one in play.
  */
object LazyTabsEndpoints:

    // snippet: lazy-tabs-endpoints
    /** The route a tab click hits; `tabRoute.action(3)` reverse-routes to `@get('/lazy-tabs/3')`.
      */
    val tabRoute: PublicEndpoint[Int, Unit, Unit, Any] =
        endpoint.get.in("lazy-tabs" / path[Int]("index"))

    /** The server endpoint: streams the whole tab widget for the selected index. */
    val tab: PublicEndpoint[Int, Unit, Stream[Throwable, Byte], ZioStreams] =
        tabRoute.out(datastarEvents)
    // snippet-end

end LazyTabsEndpoints
