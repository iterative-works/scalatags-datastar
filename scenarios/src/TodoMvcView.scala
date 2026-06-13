// PURPOSE: The TodoMVC widget — the new-todo input, the filtered list, and the footer controls.
// PURPOSE: Shared item/count fragments render both the initial list and every granular SSE patch.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The TodoMVC example's live fragment.
  *
  * The new-todo input binds `$input` and adds on Enter; the list lazy-loads on init and is patched
  * granularly (`#todo-list` inner, `#todo-count` outer) by each action. The filter links set the
  * `$mode` signal and re-fetch the filtered list, highlighting the active one client-side with
  * `data-class`. Every mutating action reverse-routes a typed endpoint — add (`@post`), toggle
  * (`@put`), delete (`@delete`), toggle-all and clear-completed (`@put`).
  */
object TodoMvcView:

    private val listAction: String = TodoMvcEndpoints.listRoute.action
    private val addAction: String = TodoMvcEndpoints.addRoute.action
    private def toggleAction(id: Long): String = TodoMvcEndpoints.toggleRoute.action(id)
    private def deleteAction(id: Long): String = TodoMvcEndpoints.deleteRoute.action(id)
    private val toggleAllAction: String = TodoMvcEndpoints.toggleAllRoute.action
    private val clearAction: String = TodoMvcEndpoints.clearRoute.action

    // snippet: todomvc-view
    /** One todo `<li>`: a server-driven toggle, the text, and a delete button. */
    def todoItem(todo: Todo): Frag =
        li(cls := (if todo.completed then "completed" else ""))(
            input(
                `type` := "checkbox",
                cls := "toggle",
                if todo.completed then checked := "checked" else frag(),
                dataOn("click").prevent := toggleAction(todo.id)
            ),
            label(todo.text),
            button(cls := "destroy", dataOn("click") := deleteAction(todo.id))("×")
        )

    def items(todos: Seq[Todo]): Frag = frag(todos.map(todoItem))

    /** The "N items left" counter, keyed `todo-count` so the patch replaces it. */
    def count(active: Int): Frag =
        span(id := "todo-count")(s"$active ${if active == 1 then "item" else "items"} left")

    private def filterLink(label0: String, mode: String): Frag =
        a(
            href := "#",
            dataClass("selected") := (TodoMvc.mode === lit(mode)),
            dataOn("click").prevent := s"$$mode = '$mode'; $listAction"
        )(label0)

    val demo: Frag =
        div(dataSignals := TodoMvc())(
            tag("section")(cls := "todoapp")(
                tag("header")(cls := "header")(
                    input(
                        id := "new-todo",
                        placeholder := "What needs to be done?",
                        dataBind := TodoMvc.input,
                        dataOn("keydown") := s"evt.key === 'Enter' && $$input.trim() && $addAction"
                    )
                ),
                tag("section")(cls := "main")(
                    input(
                        id := "toggle-all",
                        `type` := "checkbox",
                        dataOn("click") := toggleAllAction
                    ),
                    label(`for` := "toggle-all")("Mark all as complete"),
                    ul(id := "todo-list", dataInit := listAction)
                ),
                tag("footer")(cls := "footer")(
                    count(0),
                    div(cls := "filters")(
                        filterLink("All", "all"),
                        filterLink("Active", "active"),
                        filterLink("Completed", "completed")
                    ),
                    button(cls := "clear-completed", dataOn("click") := clearAction)(
                        "Clear completed"
                    )
                )
            )
        )
    // snippet-end

end TodoMvcView
