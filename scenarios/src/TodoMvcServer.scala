// PURPOSE: Server logic for the TodoMVC example — mutates the todo repository, patches list + count.
// PURPOSE: Every action renders the filtered list and the active count; add also clears the input.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.ztapir.*
import sttp.capabilities.zio.ZioStreams
import org.http4s.HttpRoutes
import zio.*
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The TodoMVC example's handlers.
  *
  * Every action mutates the shared todo repository, then re-renders two regions: the filtered list
  * (`#todo-list` inner) and the active count (`#todo-count`). Add additionally clears the input
  * signal. The filter mode arrives on each request as a client signal, so the server always renders
  * the list the page is currently showing.
  */
object TodoMvcServer:

    /** The list + count patch events for the current todos under `mode`. */
    private def renderEvents(todos: Seq[Todo], mode: String): Seq[String] =
        Seq(
            ServerSentEvents.patchElements(
                TodoMvcView.items(Todos.filtered(todos, mode)),
                selector = Some("#todo-list"),
                mode = ElementPatchMode.Inner
            ),
            ServerSentEvents.patchElements(TodoMvcView.count(Todos.activeCount(todos)))
        )

    /** Re-render the list and count after any mutation. */
    private def render(mode: String): UIO[Stream[Throwable, Byte]] =
        Todos.repo.all.map(todos => datastarStream(renderEvents(todos, mode)*))

    // snippet: todomvc-server
    private val listLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.list.zServerLogic(store => render(store.mode))

    private val addLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.add.zServerLogic: store =>
            val text = store.input.trim
            for
                _ <- ZIO.when(text.nonEmpty):
                    Todos.repo.all.flatMap(todos =>
                        Todos.repo.add(Todo(Todos.nextId(todos), text, completed = false))
                    )
                todos <- Todos.repo.all
            yield datastarStream(
                (renderEvents(todos, store.mode) :+ ServerSentEvents.patchSignalsRaw(
                    """{"input":""}"""
                ))*
            )
            end for

    private val toggleLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.toggle.zServerLogic: input =>
            val (id, store) = input
            Todos.repo.update(id)(todo => todo.copy(completed = !todo.completed)) *> render(
                store.mode
            )

    private val deleteLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.delete.zServerLogic: input =>
            val (id, store) = input
            Todos.repo.delete(id) *> render(store.mode)

    private val toggleAllLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.toggleAll.zServerLogic: store =>
            for
                todos <- Todos.repo.all
                _ <- Todos.repo.updateAll(_.copy(completed = !todos.forall(_.completed)))
                events <- render(store.mode)
            yield events

    private val clearLogic: ZServerEndpoint[Any, ZioStreams] =
        TodoMvcEndpoints.clear.zServerLogic: store =>
            for
                todos <- Todos.repo.all
                _ <- ZIO.foreachDiscard(todos.filter(_.completed).map(_.id))(Todos.repo.delete)
                events <- render(store.mode)
            yield events
    // snippet-end

    val serverEndpoints: List[ZServerEndpoint[Any, ZioStreams]] =
        List(listLogic, addLogic, toggleLogic, deleteLogic, toggleAllLogic, clearLogic)

    val routes: HttpRoutes[[A] =>> RIO[Any, A]] =
        HttpServer.routes(serverEndpoints)

    /** Binds a Blaze server serving just TodoMVC to `host`/`port`, scoped. */
    def serve(port: Int, host: String = "localhost"): RIO[Scope, org.http4s.server.Server] =
        HttpServer.serve(serverEndpoints, port, host)

end TodoMvcServer
