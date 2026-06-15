// PURPOSE: The TodoMVC example's domain — the todo row, the client store, and the shared repository.
// PURPOSE: Todos are server state in a Repository; the input and filter mode are client signals.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

// snippet: todomvc-store
/** One todo item — server state held in the repository. */
final case class Todo(id: Long, text: String, completed: Boolean)

/** The page's client signal store: the new-todo input text and the active filter mode. The todos
  * themselves are server state; only these view-state signals ride each action's request.
  */
final case class TodoMvc(input: String = "", mode: String = "all")
    derives Signals, JsonEncoder, JsonDecoder
object TodoMvc extends Signals.Handles[TodoMvc]:
    val input = signal("input")
    val mode = signal("mode")

/** The TodoMVC example's store: a repository of todos plus the pure filter/derive helpers. */
object Todos:

    val seed: Seq[Todo] = Seq(
        Todo(1, "Taste Datastar", completed = true),
        Todo(2, "Build a hypermedia app", completed = false),
        Todo(3, "Profit", completed = false)
    )

    val repo: Repository[Long, Todo] = Repository(seed, _.id)

    /** The todos visible under a filter mode (`active`, `completed`, or all). */
    def filtered(todos: Seq[Todo], mode: String): Seq[Todo] =
        mode match
            case "active"    => todos.filterNot(_.completed)
            case "completed" => todos.filter(_.completed)
            case _           => todos

    /** How many todos are not yet completed. */
    def activeCount(todos: Seq[Todo]): Int = todos.count(!_.completed)

    /** The id to give the next added todo. */
    def nextId(todos: Seq[Todo]): Long = todos.map(_.id).maxOption.getOrElse(0L) + 1
// snippet-end

end Todos
