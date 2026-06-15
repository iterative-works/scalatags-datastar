// PURPOSE: Pins how a Datastar action's options object (@post('/u', {contentType, headers})) renders.
// PURPOSE: Covers content type, header key-quoting and value escaping, ordering, and the empty case.
package works.iterative.scalatags.datastar.tapir

import scalatags.Text.all.*
import sttp.tapir.*
import utest.*
import works.iterative.scalatags.datastar.Datastar.*

object ActionOptionsTest extends TestSuite:

    val post = endpoint.post.in("todos")
    val get = endpoint.get.in("x")

    val tests = Tests {

        test("no options renders a bare action") {
            assert(post.action == "@post('/todos')")
            assert(post.action(ActionOptions.empty) == "@post('/todos')")
        }

        test("form content type renders the options object") {
            assert(post.action(ActionOptions.form) == "@post('/todos', {contentType: 'form'})")
        }

        test("json content type is rendered explicitly when set") {
            assert(get.action(ActionOptions.json) == "@get('/x', {contentType: 'json'})")
        }

        test("a header renders a nested object with a quoted key") {
            val opts = ActionOptions.empty.withHeader("X-CSRF-Token", "abc123")
            assert(
                post.action(opts) == "@post('/todos', {headers: {'X-CSRF-Token': 'abc123'}})"
            )
        }

        test("content type and headers render together, content type first") {
            val opts = ActionOptions.form.withHeader("X-CSRF-Token", "abc123")
            assert(
                post.action(opts) ==
                    "@post('/todos', {contentType: 'form', headers: {'X-CSRF-Token': 'abc123'}})"
            )
        }

        test("multiple headers keep insertion order") {
            val opts = ActionOptions.empty.withHeader("A", "1").withHeader("B", "2")
            assert(post.action(opts) == "@post('/todos', {headers: {'A': '1', 'B': '2'}})")
        }

        test("an apostrophe in a header value is escaped, not breaking the literal") {
            val opts = ActionOptions.empty.withHeader("X-User", "o'brien")
            assert(post.action(opts) == """@post('/todos', {headers: {'X-User': 'o\'brien'}})""")
        }

        test("a backslash in a header value is escaped") {
            val opts = ActionOptions.empty.withHeader("X-Path", "a\\b")
            assert(post.action(opts) == """@post('/todos', {headers: {'X-Path': 'a\\b'}})""")
        }

        test("options compose into data-on inside an element") {
            val save = post.action(ActionOptions.form)
            val rendered = button(dataOn("click") := save)("Save").render
            assert(
                rendered ==
                    """<button data-on:click="@post('/todos', {contentType: 'form'})">Save</button>"""
            )
        }
    }

end ActionOptionsTest
