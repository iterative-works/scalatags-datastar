// PURPOSE: Server logic for the bulk-update example — flips the selected accounts' status.
// PURPOSE: Maps the selection array to row ids in render order, updates the store, re-renders.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The bulk-update example's handlers.
  *
  * [[rowsLogic]] renders the repository into the table. The bulk actions map the selection array to
  * row ids in render order, set those accounts active or inactive, and patch the whole table back.
  */
object BulkUpdateServer:

    private def renderTable(accounts: Seq[Account]): Stream[Throwable, Byte] =
        datastarStream(
            ServerSentEvents.patchElements(
                BulkUpdateView.rows(accounts),
                selector = Some("#accounts"),
                mode = ElementPatchMode.Inner
            )
        )

    /** Sets the selected rows (in render order) to `active`, then re-renders the table. */
    private def apply(active: Boolean)(selection: BulkSelection): UIO[Stream[Throwable, Byte]] =
        for
            accounts <- Accounts.repo.all
            chosen = accounts.zipWithIndex.collect:
                case (account, index) if selection.selections.lift(index).contains(true) =>
                    account.id
            _ <- ZIO.foreachDiscard(chosen)(id => Accounts.repo.update(id)(_.copy(active = active)))
            updated <- Accounts.repo.all
        yield renderTable(updated)

    // snippet: bulk-update-server
    private val rowsLogic: ZServerEndpoint[Any, ZioStreams] =
        BulkUpdateEndpoints.rows.zServerLogic(_ => Accounts.repo.all.map(renderTable))

    private val activateLogic: ZServerEndpoint[Any, ZioStreams] =
        BulkUpdateEndpoints.activate.zServerLogic(apply(active = true))

    private val deactivateLogic: ZServerEndpoint[Any, ZioStreams] =
        BulkUpdateEndpoints.deactivate.zServerLogic(apply(active = false))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(rowsLogic, activateLogic, deactivateLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just bulk-update to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end BulkUpdateServer
