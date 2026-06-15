// PURPOSE: Proves a Tapir endpoint action composes with the core `dataOn` binding end-to-end.
// PURPOSE: A template references an endpoint value and renders the verb + reverse-routed URL.
package works.iterative.scalatags.datastar.tapir

import scalatags.Text.all.*
import sttp.tapir.*
import utest.*
import works.iterative.scalatags.datastar.Datastar.*

import scala.concurrent.duration.*

object ActionIntegrationTest extends TestSuite:

    // Endpoints defined once; templates can only reference these, and the URL/verb follow the route.
    val toggleTodo = endpoint.post.in("todos" / path[Long]("id") / "toggle")
    val searchTodos = endpoint.get.in("todos").in(query[String]("q"))

    val tests = Tests {

        test("action wires into data-on and renders verb plus reverse-routed url") {
            val rendered = button(dataOn("click") := toggleTodo.action(7L))("Toggle").render
            assert(
                rendered == """<button data-on:click="@post('/todos/7/toggle')">Toggle</button>"""
            )
        }

        test("action composes with data-on modifiers") {
            val rendered =
                input(dataOn("input").debounce(300.millis) := searchTodos.action("milk")).render
            assert(
                rendered == """<input data-on:input__debounce.300ms="@get('/todos?q=milk')" />"""
            )
        }
    }

end ActionIntegrationTest
