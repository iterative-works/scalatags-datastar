// PURPOSE: Unit tests for the bulk-update example — the selection array binding and bulk actions.
// PURPOSE: Pins the shared data-bind:selections array, the @put toolbar actions, and the lazy table.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object BulkUpdateViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds an empty selections array and offers the bulk actions"):
            val html = BulkUpdateView.demo.render
            assert(html.contains("""data-signals="{selections: []}""""))
            assert(html.contains("""data-on:click="@put('/bulk-update/activate')""""))
            assert(html.contains("""data-on:click="@put('/bulk-update/deactivate')""""))
            assert(html.contains("""data-init="@get('/bulk-update/rows')""""))

        test("a row binds its checkbox into the shared selections array"):
            val html =
                BulkUpdateView.row(Account(1, "Joe", "joe@example.com", active = false)).render
            assert(html.contains("""id="account-1""""))
            assert(html.contains("""data-bind:selections="""""))
            assert(html.contains("Inactive"))

        test("every row binds the same selections signal, so Datastar fills it by row order"):
            val html = BulkUpdateView.rows(Accounts.seed).render
            assert(html.split("""data-bind:selections="""").length - 1 == Accounts.seed.size)

    end tests

end BulkUpdateViewTest
