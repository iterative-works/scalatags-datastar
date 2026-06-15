// PURPOSE: Tapir endpoints for the active-search example — the GET search action and its server form.
// PURPOSE: A GET action carries the signal store in the `datastar` query param, not a request body.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The active-search example's action route.
  *
  * Two channels: [[searchRoute]] is the empty-input route the template's `@get(...)` action
  * reverse-routes, and [[search]] is its server realisation, built from the same route by adding
  * the `datastar` query parameter (decoded with `readSignals` into the typed `ActiveSearch`) and
  * the SSE output.
  */
object ActiveSearchEndpoints:

    // snippet: active-search-endpoints
    /** The route a debounced keystroke hits; reverse-routes to `@get('/active-search/search')`. */
    val searchRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("active-search" / "search")

    /** The server endpoint: the round-tripped store arrives in the `datastar` query parameter —
      * decoded into the typed `ActiveSearch` (a misfit parameter is a `400`) — and the response
      * streams a `patch-elements` event as `text/event-stream`.
      */
    val search: PublicEndpoint[ActiveSearch, Unit, Stream[Throwable, Byte], ZioStreams] =
        searchRoute
            .in(SignalsInput.query[ActiveSearch])
            .out(datastarEvents)
    // snippet-end

end ActiveSearchEndpoints
