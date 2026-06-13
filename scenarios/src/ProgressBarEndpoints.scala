// PURPOSE: Tapir endpoints for the progress-bar example — the GET feed and its SSE server form.
// PURPOSE: A one-way feed: no signals are read; the route only opens the event-stream.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The progress-bar example's route: [[updatesRoute]] is what `data-init` reverse-routes, and
  * [[updates]] is its server realisation streaming the progress feed.
  */
object ProgressBarEndpoints:

    // snippet: progress-bar-endpoints
    /** The route `data-init` opens; reverse-routes to `@get('/progress-bar/updates')`. */
    val updatesRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("progress-bar" / "updates")

    /** The server endpoint: streams the progress updates as `text/event-stream`. */
    val updates: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        updatesRoute.out(datastarEvents)
    // snippet-end

end ProgressBarEndpoints
