// PURPOSE: Verifies Tapir endpoints render to Datastar backend-action expressions (@get('/url'), …).
// PURPOSE: Pins verb derivation from the endpoint method and JS-string escaping of the embedded URL.
package works.iterative.scalatags.datastar.tapir

import utest.*
import sttp.tapir.*
import EndpointAction.action

object EndpointActionTest extends TestSuite:

    val tests = Tests {

        test("verb is derived from the endpoint method") {
            val get = endpoint.get.in("users" / path[Int]("id"))
            val post = endpoint.post.in("todos")
            val put = endpoint.put.in("todos" / path[Int]("id"))
            val patch = endpoint.patch.in("todos" / path[Int]("id"))
            val del = endpoint.delete.in("todos" / path[Int]("id"))
            assert(action(get).map(_(42)) == Some("@get('/users/42')"))
            assert(action(post).map(_(())) == Some("@post('/todos')"))
            assert(action(put).map(_(1)) == Some("@put('/todos/1')"))
            assert(action(patch).map(_(1)) == Some("@patch('/todos/1')"))
            assert(action(del).map(_(1)) == Some("@delete('/todos/1')"))
        }

        test("query params carry into the action url") {
            val ep = endpoint.get.in("search").in(query[String]("q"))
            assert(action(ep).map(_("hello")) == Some("@get('/search?q=hello')"))
        }

        test("endpoint with no fixed method has no action") {
            val ep = endpoint.in("x")
            assert(action(ep) == None)
        }

        test("method Datastar has no action for yields none") {
            val ep = endpoint.head.in("x")
            assert(action(ep) == None)
        }

        test("apostrophe in a value is escaped, not breaking the string literal") {
            val ep = endpoint.get.in("u" / path[String]("s"))
            assert(action(ep).map(_("o'brien")) == Some("""@get('/u/o\'brien')"""))
        }

        test("a lone apostrophe cannot close the string and inject syntax") {
            // Were the apostrophe not escaped, this would be @get('/search?q='') — an empty url
            // followed by stray syntax. Escaped, it stays a single string holding the literal value.
            val ep = endpoint.get.in("search").in(query[String]("q"))
            assert(action(ep).map(_("'")) == Some("""@get('/search?q=\'')"""))
        }
    }

end EndpointActionTest
