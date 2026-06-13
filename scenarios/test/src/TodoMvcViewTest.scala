// PURPOSE: Unit tests for the TodoMVC example — the bound input, the list, the filters, an item.
// PURPOSE: Pins the add-on-Enter expression, the typed per-item actions, and the count pluralisation.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object TodoMvcViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds the input and mode signals and binds the new-todo input"):
            val html = TodoMvcView.demo.render
            assert(html.contains("""data-signals="{input: '', mode: 'all'}""""))
            assert(html.contains("""data-bind="input""""))

        test("Enter on the new-todo input adds via the reverse-routed post"):
            val html = TodoMvcView.demo.render
            assert(html.contains("evt.key === 'Enter'"))
            assert(html.contains("@post('/todomvc/add')"))

        test("the list lazy-loads and the controls wire toggle-all and clear"):
            val html = TodoMvcView.demo.render
            assert(html.contains("""id="todo-list""""))
            assert(html.contains("""data-init="@get('/todomvc/list')""""))
            assert(html.contains("""data-on:click="@put('/todomvc/toggle-all')""""))
            assert(html.contains("""data-on:click="@put('/todomvc/clear-completed')""""))

        test("the filter links set the mode signal and highlight the active one"):
            val html = TodoMvcView.demo.render
            assert(html.contains("""data-class:selected="$mode === 'active'""""))
            assert(html.contains("$mode = 'active'; @get('/todomvc/list')"))

        test("a completed todo renders checked with the typed toggle and delete actions"):
            val html = TodoMvcView.todoItem(Todo(1, "Taste Datastar", completed = true)).render
            assert(html.contains("""class="completed""""))
            assert(html.contains("checked"))
            assert(html.contains("@put('/todomvc/1/toggle')"))
            assert(html.contains("@delete('/todomvc/1')"))

        test("the count pluralises correctly"):
            assert(TodoMvcView.count(1).render.contains("1 item left"))
            assert(TodoMvcView.count(3).render.contains("3 items left"))

    end tests

end TodoMvcViewTest
