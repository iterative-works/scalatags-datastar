// PURPOSE: Proves a Tapir endpoint action composes with the core `dataOn` binding end-to-end.
// PURPOSE: A template references an endpoint value and renders the verb + reverse-routed URL.
package works.iterative.scalatags.datastar.tapir

import utest.*
import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import sttp.tapir.*
import scala.concurrent.duration.*
import EndpointAction.action

object ActionIntegrationTest extends TestSuite:

    // Endpoints defined once; templates can only reference these, and the URL/verb follow the route.
    val toggleTodo = endpoint.post.in("todos" / path[Long]("id") / "toggle")
    val searchTodos = endpoint.get.in("todos").in(query[String]("q"))

    val tests = Tests {

        test("action wires into data-on and renders verb plus reverse-routed url") {
            val toggle = action(toggleTodo).get
            val rendered = button(dataOn("click") := toggle(7L))("Toggle").render
            assert(
                rendered == """<button data-on:click="@post('/todos/7/toggle')">Toggle</button>"""
            )
        }

        test("action composes with data-on modifiers") {
            val search = action(searchTodos).get
            val rendered =
                input(dataOn("input").debounce(300.millis) := search("milk")).render
            assert(
                rendered == """<input data-on:input__debounce.300ms="@get('/todos?q=milk')" />"""
            )
        }
    }

end ActionIntegrationTest
