// PURPOSE: Tapir endpoints for the animations example — the throb feed, the toggle, the fade actions.
// PURPOSE: Four routes the technique widgets reverse-route, each paired with its SSE server form.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The animations example's routes — one per technique. Each `*Route` is the empty-input route a
  * widget reverse-routes; each server form adds the SSE output (and, for view transitions, the
  * `datastar` query parameter carrying the store).
  */
object AnimationsEndpoints:

    // snippet: animations-endpoints
    /** Color throb: `data-init` opens this feed; reverse-routes to
      * `@get('/animations/color-throb')`.
      */
    val colorThrobRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("animations" / "color-throb")

    val colorThrob: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        colorThrobRoute.out(datastarEvents)

    /** View transitions: the button reverse-routes `@get('/animations/view-transition')`; the store
      * rides in the `datastar` query parameter so the server knows which state to swap to.
      */
    val viewTransitionRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("animations" / "view-transition")

    val viewTransition: PublicEndpoint[ViewTransition, Unit, Stream[Throwable, Byte], ZioStreams] =
        viewTransitionRoute
            .in(SignalsInput.query[ViewTransition])
            .out(datastarEvents)

    /** Fade out on swap: reverse-routes `@delete('/animations/fade-out')`. */
    val fadeOutRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.delete.in("animations" / "fade-out")

    val fadeOut: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        fadeOutRoute.out(datastarEvents)

    /** Fade in on addition: reverse-routes `@get('/animations/fade-in')`. */
    val fadeInRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("animations" / "fade-in")

    val fadeIn: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        fadeInRoute.out(datastarEvents)
    // snippet-end

end AnimationsEndpoints
