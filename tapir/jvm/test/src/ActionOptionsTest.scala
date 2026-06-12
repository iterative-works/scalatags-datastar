// PURPOSE: Pins how a Datastar action's options object (@post('/u', {contentType, headers})) renders.
// PURPOSE: Covers content type, header key-quoting and value escaping, ordering, and the empty case.
package works.iterative.scalatags.datastar.tapir

import utest.*
import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.ActionOptions
import sttp.tapir.*
import EndpointAction.action

object ActionOptionsTest extends TestSuite:

    val post = endpoint.post.in("todos")
    val get = endpoint.get.in("x")

    val tests = Tests {

        test("no options renders a bare action") {
            assert(action(post)(()) == "@post('/todos')")
            assert(action(post, ActionOptions.empty)(()) == "@post('/todos')")
        }

        test("form content type renders the options object") {
            assert(action(post, ActionOptions.form)(()) == "@post('/todos', {contentType: 'form'})")
        }

        test("json content type is rendered explicitly when set") {
            assert(action(get, ActionOptions.json)(()) == "@get('/x', {contentType: 'json'})")
        }

        test("a header renders a nested object with a quoted key") {
            val opts = ActionOptions.empty.withHeader("X-CSRF-Token", "abc123")
            assert(
                action(post, opts)(()) == "@post('/todos', {headers: {'X-CSRF-Token': 'abc123'}})"
            )
        }

        test("content type and headers render together, content type first") {
            val opts = ActionOptions.form.withHeader("X-CSRF-Token", "abc123")
            assert(
                action(post, opts)(()) ==
                    "@post('/todos', {contentType: 'form', headers: {'X-CSRF-Token': 'abc123'}})"
            )
        }

        test("multiple headers keep insertion order") {
            val opts = ActionOptions.empty.withHeader("A", "1").withHeader("B", "2")
            assert(action(post, opts)(()) == "@post('/todos', {headers: {'A': '1', 'B': '2'}})")
        }

        test("an apostrophe in a header value is escaped, not breaking the literal") {
            val opts = ActionOptions.empty.withHeader("X-User", "o'brien")
            assert(action(post, opts)(()) == """@post('/todos', {headers: {'X-User': 'o\'brien'}})""")
        }

        test("a backslash in a header value is escaped") {
            val opts = ActionOptions.empty.withHeader("X-Path", "a\\b")
            assert(action(post, opts)(()) == """@post('/todos', {headers: {'X-Path': 'a\\b'}})""")
        }

        test("options compose into data-on inside an element") {
            val save = action(post, ActionOptions.form)
            val rendered = button(dataOn("click") := save(()))("Save").render
            assert(
                rendered ==
                    """<button data-on:click="@post('/todos', {contentType: 'form'})">Save</button>"""
            )
        }
    }

end ActionOptionsTest
