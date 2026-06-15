// PURPOSE: Tapir endpoints for the click-to-load example — the GET page action and its SSE form.
// PURPOSE: The offset rides the `datastar` query param; the path itself carries no parameters.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The click-to-load example's route: [[moreRoute]] is what the Load-more button reverse-routes,
  * and [[more]] is its server realisation reading the offset from the `datastar` query parameter.
  */
object ClickToLoadEndpoints:

    // snippet: click-to-load-endpoints
    /** The route the button hits; reverse-routes to `@get('/click-to-load/more')`. */
    val moreRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("click-to-load" / "more")

    /** The server endpoint: the offset arrives in the `datastar` query parameter, decoded into the
      * typed `ClickToLoad`; the response appends the next page and patches the offset forward.
      */
    val more: PublicEndpoint[ClickToLoad, Unit, Stream[Throwable, Byte], ZioStreams] =
        moreRoute
            .in(SignalsInput.query[ClickToLoad])
            .out(datastarEvents)
    // snippet-end

end ClickToLoadEndpoints
