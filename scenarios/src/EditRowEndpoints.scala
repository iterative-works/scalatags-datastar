// PURPOSE: Tapir endpoints for the edit-row example — load, enter-edit, cancel, and a PUT save.
// PURPOSE: The id is a typed Long path parameter; Save reads the edited fields from a form body.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The edit-row example's routes: load the table, enter edit mode for one row, cancel back to the
  * read view, and save the edited fields. The id is a typed `Long` path parameter throughout;
  * [[save]] reverse-routes with `ActionOptions.form`, so the edit form is sent form-encoded.
  */
object EditRowEndpoints:

    // snippet: edit-row-endpoints
    /** Loads the current rows; reverse-routes to `@get('/edit-row/rows')`. */
    val rowsRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("edit-row" / "rows")

    /** Enter edit mode for one row; `editRoute.action(2)` -> `@get('/edit-row/2/edit')`. */
    val editRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.get.in("edit-row" / path[Long]("id") / "edit")

    /** Cancel back to the read view; `cancelRoute.action(2)` -> `@get('/edit-row/2')`. */
    val cancelRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.get.in("edit-row" / path[Long]("id"))

    /** Save the edited fields; `saveRoute.action(2, form)` -> `@put('/edit-row/2', {contentType:
      * 'form'})`.
      */
    val saveRoute: PublicEndpoint[Long, Unit, Unit, Any] =
        endpoint.put.in("edit-row" / path[Long]("id"))

    val rows: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        rowsRoute.out(datastarEvents)

    val edit: PublicEndpoint[Long, Unit, Stream[Throwable, Byte], ZioStreams] =
        editRoute.out(datastarEvents)

    val cancel: PublicEndpoint[Long, Unit, Stream[Throwable, Byte], ZioStreams] =
        cancelRoute.out(datastarEvents)

    val save
        : PublicEndpoint[(Long, Seq[(String, String)]), Unit, Stream[Throwable, Byte], ZioStreams] =
        saveRoute.in(formBody[Seq[(String, String)]]).out(datastarEvents)
    // snippet-end

end EditRowEndpoints
