// PURPOSE: Verifies Tapir endpoints render to Datastar backend-action expressions (@get('/url'), …).
// PURPOSE: Pins verb derivation (with GET fallback) and JS-string escaping of the embedded URL.
package works.iterative.scalatags.datastar.tapir

import utest.*
import sttp.tapir.*

object EndpointActionTest extends TestSuite:

    val tests = Tests {

        test("verb is derived from the endpoint method") {
            val get = endpoint.get.in("users" / path[Int]("id"))
            val post = endpoint.post.in("todos")
            val put = endpoint.put.in("todos" / path[Int]("id"))
            val patch = endpoint.patch.in("todos" / path[Int]("id"))
            val del = endpoint.delete.in("todos" / path[Int]("id"))
            assert(get.action(42) == "@get('/users/42')")
            assert(post.action == "@post('/todos')")
            assert(put.action(1) == "@put('/todos/1')")
            assert(patch.action(1) == "@patch('/todos/1')")
            assert(del.action(1) == "@delete('/todos/1')")
        }

        test("query params carry into the action url") {
            val ep = endpoint.get.in("search").in(query[String]("q"))
            assert(ep.action("hello") == "@get('/search?q=hello')")
        }

        test("endpoint with no fixed method falls back to get") {
            // Mirrors Tapir's own client interpreter, which defaults a methodless endpoint to GET.
            val ep = endpoint.in("x")
            assert(ep.action == "@get('/x')")
        }

        test("method Datastar has no action for falls back to get") {
            // Datastar has no @head action; GET is the closest expressible action.
            val ep = endpoint.head.in("x")
            assert(ep.action == "@get('/x')")
        }

        test("apostrophe in a value is escaped, not breaking the string literal") {
            val ep = endpoint.get.in("u" / path[String]("s"))
            assert(ep.action("o'brien") == """@get('/u/o\'brien')""")
        }

        test("a lone apostrophe cannot close the string and inject syntax") {
            // Were the apostrophe not escaped, this would be @get('/search?q='') — an empty url
            // followed by stray syntax. Escaped, it stays a single string holding the literal value.
            val ep = endpoint.get.in("search").in(query[String]("q"))
            assert(ep.action("'") == """@get('/search?q=\'')""")
        }
    }

end EndpointActionTest
