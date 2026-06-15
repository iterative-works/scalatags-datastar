// PURPOSE: Server logic for the delete-row example — renders current rows and removes one by id.
// PURPOSE: Reads/mutates the shared Members repository; the delete patches the row out by id.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import scalatags.Text.all.frag
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

/** The delete-row example's handlers.
  *
  * [[rowsLogic]] renders the repository's current members into the table body (an inner patch of
  * `#members`), so the lazily-loaded table always reflects the live store. [[deleteLogic]] removes
  * the member from the store and patches the row out of the DOM by id (`mode = remove`).
  */
object DeleteRowServer:

    // snippet: delete-row-server
    private val rowsLogic: ZServerEndpoint[Any, ZioStreams] =
        DeleteRowEndpoints.rows.zServerLogic: _ =>
            Members.repo.all.map: members =>
                datastarStream(
                    ServerSentEvents.patchElements(
                        DeleteRowView.rows(members),
                        selector = Some("#members"),
                        mode = ElementPatchMode.Inner
                    )
                )

    private val deleteLogic: ZServerEndpoint[Any, ZioStreams] =
        DeleteRowEndpoints.delete.zServerLogic: id =>
            Members.repo.delete(id).as(
                datastarStream(
                    ServerSentEvents.patchElements(
                        frag(),
                        selector = Some(s"#member-$id"),
                        mode = ElementPatchMode.Remove
                    )
                )
            )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(rowsLogic, deleteLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just delete-row to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end DeleteRowServer
