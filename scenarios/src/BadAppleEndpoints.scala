// PURPOSE: Tapir endpoint for the bad-apple example — the GET feed that streams ASCII frames.
// PURPOSE: One-way: no signal store; the route only opens the event-stream.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The bad-apple example's route: [[playRoute]] is what data-init reverse-routes, [[play]] its
  * server realisation streaming the frames.
  */
object BadAppleEndpoints:

    // snippet: bad-apple-endpoints
    /** The route data-init opens; reverse-routes to `@get('/bad-apple/play')`. */
    val playRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("bad-apple" / "play")

    val play: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        playRoute.out(datastarEvents)
    // snippet-end

end BadAppleEndpoints
