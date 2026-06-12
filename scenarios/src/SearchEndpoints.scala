// PURPOSE: Tapir endpoints for the live-search example — the GET search action and its server form.
// PURPOSE: A GET action carries the signal store in the `datastar` query param, not a request body.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The live-search example's action route.
  *
  * Two channels again, but a `@get` action transports the signal store differently than a `@post`
  * one: the Datastar client serialises the signals into a `datastar` query parameter rather than a
  * request body. So [[searchRoute]] is the empty-input route the template's `@get(...)` action
  * reverse-routes, and [[search]] is its server realisation, built from the same route by adding
  * the `datastar` query parameter (decoded with `readSignals`) and the SSE output.
  */
object SearchEndpoints:

    // snippet: search-endpoints
    /** The route a debounced keystroke hits. Empty input — the template action reverse-routes this
      * to `@get('/search/results')`.
      */
    val searchRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("search" / "results")

    /** The server endpoint: the round-tripped signal store arrives URL-encoded in the `datastar`
      * query parameter — decoded into the typed `Search` by [[SignalsInput.query]], so a parameter
      * that does not fit the store is already a `400` and the handler only sees a valid one — and
      * the response streams a `patch-elements` event as `text/event-stream`.
      */
    val search: PublicEndpoint[Search, Unit, Stream[Throwable, Byte], ZioStreams] =
        searchRoute
            .in(SignalsInput.query[Search])
            .out(datastarEvents)
    // snippet-end

end SearchEndpoints
