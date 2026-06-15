// PURPOSE: Tapir endpoints for the click-to-edit example — view, edit, save (PUT), reset (PATCH).
// PURPOSE: Save carries the profile signals in its body; the others read the server record.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The click-to-edit example's routes. [[viewRoute]] (also the data-init loader and the Cancel
  * target) and [[editRoute]] read the server record; [[saveRoute]] is a `@put` carrying the edited
  * signals; [[resetRoute]] is a `@patch` that restores the original.
  */
object ClickToEditEndpoints:

    // snippet: click-to-edit-endpoints
    val viewRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("click-to-edit" / "view")

    val editRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.get.in("click-to-edit" / "edit")

    /** Save the edited profile; reverse-routes to `@put('/click-to-edit/save')`. */
    val saveRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.put.in("click-to-edit" / "save")

    /** Reset to the original; reverse-routes to `@patch('/click-to-edit/reset')`. */
    val resetRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.patch.in("click-to-edit" / "reset")

    val view: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        viewRoute.out(datastarEvents)

    val edit: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        editRoute.out(datastarEvents)

    val save: PublicEndpoint[Profile, Unit, Stream[Throwable, Byte], ZioStreams] =
        saveRoute.in(SignalsInput.body[Profile]).out(datastarEvents)

    val reset: PublicEndpoint[Unit, Unit, Stream[Throwable, Byte], ZioStreams] =
        resetRoute.out(datastarEvents)
    // snippet-end

end ClickToEditEndpoints
