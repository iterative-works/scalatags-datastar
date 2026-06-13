// PURPOSE: Tapir endpoints for the bulk-update example — the rows loader and two PUT bulk actions.
// PURPOSE: The selection array rides the @put body, decoded into BulkSelection by SignalsInput.body.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The bulk-update example's routes: load the table, and activate or deactivate the selected rows.
  * The two bulk actions are `@put`s whose body carries the selection signal store.
  */
object BulkUpdateEndpoints:

    // snippet: bulk-update-endpoints
    /** Loads the current rows; reverse-routes to `@get('/bulk-update/rows')`. */
    val rowsRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("bulk-update" / "rows")

    /** Activate the selected accounts; reverse-routes to `@put('/bulk-update/activate')`. */
    val activateRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.put.in("bulk-update" / "activate")

    /** Deactivate the selected accounts; reverse-routes to `@put('/bulk-update/deactivate')`. */
    val deactivateRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.put.in("bulk-update" / "deactivate")

    val rows: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        rowsRoute.out(datastarEvents)

    val activate: PublicEndpoint[BulkSelection, Unit, Stream[Throwable, Byte], ZioStreams] =
        activateRoute.in(SignalsInput.body[BulkSelection]).out(datastarEvents)

    val deactivate: PublicEndpoint[BulkSelection, Unit, Stream[Throwable, Byte], ZioStreams] =
        deactivateRoute.in(SignalsInput.body[BulkSelection]).out(datastarEvents)
    // snippet-end

end BulkUpdateEndpoints
