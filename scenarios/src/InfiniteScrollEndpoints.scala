// PURPOSE: Tapir endpoints for the infinite-scroll example — the GET page action and its SSE form.
// PURPOSE: The offset rides the `datastar` query param; the path itself carries no parameters.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The infinite-scroll example's route: [[moreRoute]] is what the sentinel reverse-routes, and
  * [[more]] is its server realisation reading the offset from the `datastar` query parameter.
  */
object InfiniteScrollEndpoints:

    // snippet: infinite-scroll-endpoints
    /** The route the sentinel hits; reverse-routes to `@get('/infinite-scroll/more')`. */
    val moreRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("infinite-scroll" / "more")

    /** The server endpoint: the offset arrives in the `datastar` query parameter, decoded into the
      * typed `InfiniteScroll`; the response appends the next page and re-arms the sentinel.
      */
    val more: PublicEndpoint[InfiniteScroll, Unit, Stream[Throwable, Byte], ZioStreams] =
        moreRoute
            .in(SignalsInput.query[InfiniteScroll])
            .out(datastarEvents)
    // snippet-end

end InfiniteScrollEndpoints
