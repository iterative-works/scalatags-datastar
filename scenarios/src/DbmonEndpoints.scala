// PURPOSE: Tapir endpoint for the dbmon example — the GET feed carrying the rate controls.
// PURPOSE: The fps/mutationRate signals ride the datastar query param, decoded into Dbmon.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The dbmon example's route: [[updatesRoute]] is what data-init and the controls reverse-route,
  * and [[updates]] is its server realisation reading the rate controls from the `datastar` query
  * param.
  */
object DbmonEndpoints:

    // snippet: dbmon-endpoints
    /** The updates feed; reverse-routes to `@get('/dbmon/updates')`. */
    val updatesRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("dbmon" / "updates")

    val updates: PublicEndpoint[Dbmon, Unit, Stream[Throwable, Byte], ZioStreams] =
        updatesRoute.in(SignalsInput.query[Dbmon]).out(datastarEvents)
    // snippet-end

end DbmonEndpoints
