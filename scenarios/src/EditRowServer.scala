// PURPOSE: Server logic for the edit-row example — swaps a row between read and edit, saves changes.
// PURPOSE: Reads/mutates the shared People repository; each action patches just the one row by id.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import scalatags.Text.all.Frag
import org.http4s.HttpRoutes
import zio.*
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The edit-row example's handlers.
  *
  * [[rowsLogic]] renders the repository into the table body. [[editLogic]] and [[cancelLogic]]
  * patch the one row into its edit form or back to the read view. [[saveLogic]] reads the
  * form-encoded fields, updates the person in the store, and patches the read view back.
  */
object EditRowServer:

    private def patchRow(row: Frag): Stream[Throwable, Byte] =
        datastarStream(ServerSentEvents.patchElements(row))

    private def rowOf(person: Option[Person])(render: Person => Frag): Stream[Throwable, Byte] =
        person.fold(datastarStream())(p => patchRow(render(p)))

    // snippet: edit-row-server
    private val rowsLogic: ZServerEndpoint[Any, ZioStreams] =
        EditRowEndpoints.rows.zServerLogic: _ =>
            People.repo.all.map: people =>
                datastarStream(
                    ServerSentEvents.patchElements(
                        EditRowView.rows(people),
                        selector = Some("#people"),
                        mode = ElementPatchMode.Inner
                    )
                )

    private val editLogic: ZServerEndpoint[Any, ZioStreams] =
        EditRowEndpoints.edit.zServerLogic: id =>
            People.repo.get(id).map(rowOf(_)(EditRowView.editRow))

    private val cancelLogic: ZServerEndpoint[Any, ZioStreams] =
        EditRowEndpoints.cancel.zServerLogic: id =>
            People.repo.get(id).map(rowOf(_)(EditRowView.readRow))

    private val saveLogic: ZServerEndpoint[Any, ZioStreams] =
        EditRowEndpoints.save.zServerLogic: input =>
            val (id, fields) = input
            val form = fields.toMap
            val edited = (person: Person) =>
                person.copy(
                    name = form.getOrElse("name", person.name),
                    email = form.getOrElse("email", person.email)
                )
            People.repo.update(id)(edited) *>
                People.repo.get(id).map(rowOf(_)(EditRowView.readRow))
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(rowsLogic, editLogic, cancelLogic, saveLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just edit-row to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end EditRowServer
