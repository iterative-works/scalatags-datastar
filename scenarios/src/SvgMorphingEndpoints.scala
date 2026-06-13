// PURPOSE: Tapir endpoint for the svg-morphing example — the GET that streams a randomised circle.
// PURPOSE: Stateless: each morph is independent, so no signal store and no server state.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The svg-morphing example's route: [[morphRoute]] is what the button reverse-routes, and
  * [[morph]] is its server realisation streaming the new circle.
  */
object SvgMorphingEndpoints:

    // snippet: svg-morphing-endpoints
    /** The morph route; reverse-routes to `@get('/svg-morphing/morph')`. */
    val morphRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("svg-morphing" / "morph")

    val morph: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        morphRoute.out(datastarEvents)
    // snippet-end

end SvgMorphingEndpoints
