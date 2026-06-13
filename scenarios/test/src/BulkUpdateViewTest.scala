// PURPOSE: Unit tests for the bulk-update example — the selection array binding and bulk actions.
// PURPOSE: Pins the array-index data-bind, the @put toolbar actions, and the lazy-loaded table.
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

        test("a row binds its checkbox to its selections index"):
            val html = BulkUpdateView.row(Account(1, "Joe", "joe@example.com", active = false), 0).render
            assert(html.contains("""id="account-1""""))
            assert(html.contains("""data-bind="selections[0]""""))
            assert(html.contains("Inactive"))

        test("rows are indexed in render order"):
            val html = BulkUpdateView.rows(Accounts.seed).render
            assert(html.contains("""data-bind="selections[0]""""))
            assert(html.contains("""data-bind="selections[4]""""))

    end tests

end BulkUpdateViewTest
