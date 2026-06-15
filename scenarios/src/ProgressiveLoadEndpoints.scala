// PURPOSE: Tapir endpoints for the progressive-load example — the GET feed and its SSE server form.
// PURPOSE: The signal store rides the `datastar` query param (faithful two-channel), though unused.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The progressive-load example's route: [[updatesRoute]] is what the Load button reverse-routes,
  * and [[updates]] is its server realisation streaming the sections.
  */
object ProgressiveLoadEndpoints:

    // snippet: progressive-load-endpoints
    /** The route the Load button opens; reverse-routes to `@get('/progressive-load/updates')`. */
    val updatesRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("progressive-load" / "updates")

    /** The server endpoint: the store rides the `datastar` query parameter (decoded for
      * faithfulness, though the feed ignores it) and the response streams the sections.
      */
    val updates: PublicEndpoint[ProgressiveLoad, Unit, Stream[Throwable, Byte], ZioStreams] =
        updatesRoute
            .in(SignalsInput.query[ProgressiveLoad])
            .out(datastarEvents)
    // snippet-end

end ProgressiveLoadEndpoints
