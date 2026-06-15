// PURPOSE: Tapir endpoints for the title-update example — the POST action and its SSE server form.
// PURPOSE: No signals are read; the page carries none, so the route models no input.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The title-update example's route: [[updateRoute]] is what the button's `@post(...)` action
  * reverse-routes, and [[update]] is its server realisation streaming the title patch.
  */
object TitleUpdateEndpoints:

    // snippet: title-update-endpoints
    /** The route the button hits; reverse-routes to `@post('/title-update')`. */
    val updateRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("title-update")

    /** The server endpoint: streams a `patch-elements` event targeting the `<title>`. */
    val update: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        updateRoute.out(datastarEvents)
    // snippet-end

end TitleUpdateEndpoints
