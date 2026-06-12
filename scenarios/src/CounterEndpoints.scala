// PURPOSE: Tapir endpoints for the counter example — the reverse-routed action and its server form.
// PURPOSE: The signal store is never a typed input; it rides in the request body (two channels).
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The counter example's action route.
  *
  * Two channels, as the design demands: an endpoint's typed input models only explicit URL params,
  * while Datastar appends the whole signal store separately. So [[incrementRoute]] has an empty
  * input and is what the template's `@post(...)` action reverse-routes; [[increment]] is its server
  * realization, built from the same route by adding the raw signal body and the SSE output, so the
  * URL the browser calls and the handler that answers it cannot drift apart.
  */
object CounterEndpoints:

    // snippet: counter-endpoints
    /** The route a click hits. Empty input — the signal store is not modelled here; the template
      * action reverse-routes this to `@post('/increment')`.
      */
    val incrementRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("increment")

    /** The server endpoint: the round-tripped signal store arrives as the JSON request body —
      * decoded into the typed `Counter` by [[SignalsInput.body]], so a body that does not fit the
      * store is already a `400` and the handler only sees a valid one — and the response streams
      * Datastar SSE events as `text/event-stream`.
      */
    val increment: PublicEndpoint[Counter, Unit, Stream[Throwable, Byte], ZioStreams] =
        incrementRoute
            .in(SignalsInput.body[Counter])
            .out(datastarEvents)
    // snippet-end

end CounterEndpoints
