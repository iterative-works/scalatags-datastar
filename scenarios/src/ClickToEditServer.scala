// PURPOSE: Server logic for the click-to-edit example — swaps #demo and persists edits to the cell.
// PURPOSE: Edit and reset also patch the signals, so the form's bound inputs reflect the record.
package works.iterative.scalatags.datastar.scenarios

import org.http4s.HttpRoutes
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.*

/** The click-to-edit example's handlers.
  *
  * [[viewLogic]] renders the display from the record; [[editLogic]] seeds the form's signals from
  * the record and shows the form; [[saveLogic]] persists the posted profile and shows the display;
  * [[resetLogic]] restores the original record, patching both the signals and the display.
  */
object ClickToEditServer:

    // snippet: click-to-edit-server
    private val viewLogic: ZServerEndpoint[Any, ZioStreams] =
        ClickToEditEndpoints.view.zServerLogic: _ =>
            Profiles.cell.get.map(profile =>
                datastarStream(ServerSentEvents.patchElements(ClickToEditView.display(profile)))
            )

    private val editLogic: ZServerEndpoint[Any, ZioStreams] =
        ClickToEditEndpoints.edit.zServerLogic: _ =>
            Profiles.cell.get.map(profile =>
                datastarStream(
                    ServerSentEvents.patchSignals(profile),
                    ServerSentEvents.patchElements(ClickToEditView.form)
                )
            )

    private val saveLogic: ZServerEndpoint[Any, ZioStreams] =
        ClickToEditEndpoints.save.zServerLogic: profile =>
            Profiles.cell.set(profile).as(
                datastarStream(ServerSentEvents.patchElements(ClickToEditView.display(profile)))
            )

    private val resetLogic: ZServerEndpoint[Any, ZioStreams] =
        ClickToEditEndpoints.reset.zServerLogic: _ =>
            Profiles.cell.reset() *> Profiles.cell.get.map(profile =>
                datastarStream(
                    ServerSentEvents.patchSignals(profile),
                    ServerSentEvents.patchElements(ClickToEditView.display(profile))
                )
            )
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(viewLogic, editLogic, saveLogic, resetLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just click-to-edit to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end ClickToEditServer
