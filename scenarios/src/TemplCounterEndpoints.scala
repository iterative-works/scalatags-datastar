// PURPOSE: Tapir endpoints for the templ-counter example — load the shared count and increment it.
// PURPOSE: No signal store; the count lives entirely on the server.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The templ-counter example's routes: [[countRoute]] (the data-init loader) reads the shared
  * count, [[incrementRoute]] advances it.
  */
object TemplCounterEndpoints:

    // snippet: templ-counter-endpoints
    /** Loads the current shared count; reverse-routes to `@get('/templ-counter/count')`. */
    val countRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("templ-counter" / "count")

    /** Advances the shared count; reverse-routes to `@post('/templ-counter/increment')`. */
    val incrementRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("templ-counter" / "increment")

    val count: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        countRoute.out(datastarEvents)

    val increment: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        incrementRoute.out(datastarEvents)
    // snippet-end

end TemplCounterEndpoints
