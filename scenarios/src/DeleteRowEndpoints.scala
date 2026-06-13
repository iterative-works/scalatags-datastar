// PURPOSE: Tapir endpoints for the delete-row example — the rows loader and the typed DELETE action.
// PURPOSE: The member id is a typed Long path parameter; @delete reverse-routes from the same route.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The delete-row example's routes.
  *
  * [[rowsRoute]] loads the current table body (fired by the view's `data-init`); [[deleteRoute]]
  * removes a member by its typed `Long` id, which `endpoint.action(id)` reverse-routes into the
  * `@delete('/delete-row/{id}')` action on each row's button.
  */
object DeleteRowEndpoints:

    // snippet: delete-row-endpoints
    /** Loads the current rows; reverse-routes to `@get('/delete-row/rows')`. */
    val rowsRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("delete-row" / "rows")

    /** The per-member delete route; `deleteRoute.action(3)` reverse-routes to
      * `@delete('/delete-row/3')`.
      */
    val deleteRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.delete.in("delete-row" / path[Long]("id"))

    val rows: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        rowsRoute.out(datastarEvents)

    val delete: PublicEndpoint[Long, Unit, Stream[Throwable, Byte], ZioStreams] =
        deleteRoute.out(datastarEvents)
    // snippet-end

end DeleteRowEndpoints
