// PURPOSE: Tapir endpoints for the lazy-load example — the on-init GET and its SSE server form.
// PURPOSE: No signal store; the route only fetches a fragment, so it has an empty typed input.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The lazy-load example's route. The page has no signals, so the route models no input at all;
  * [[graphRoute]] is what the template's `data-init` action reverse-routes, and [[graph]] is its
  * server realisation streaming the loaded fragment.
  */
object LazyLoadEndpoints:

    // snippet: lazy-load-endpoints
    /** The route `data-init` hits; reverse-routes to `@get('/lazy-load/graph')`. */
    val graphRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("lazy-load" / "graph")

    /** The server endpoint: streams the loaded fragment as a `patch-elements` event. */
    val graph: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        graphRoute.out(datastarEvents)
    // snippet-end

end LazyLoadEndpoints
