// PURPOSE: Unit tests for the delete-row example — the lazy-loaded table and the row's delete button.
// PURPOSE: Pins the data-init loader, the keyed row id, and the confirm-guarded typed @delete action.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object DeleteRowViewTest extends TestSuite:

    val tests = Tests:

        test("the table body lazy-loads the current rows on init"):
            val html = DeleteRowView.demo.render
            assert(html.contains("""id="members""""))
            assert(html.contains("""data-init="@get('/delete-row/rows')""""))

        test("a row is keyed by id and guards a typed delete with confirm"):
            val html = DeleteRowView.row(Member(3, "Fuqua Tarkenton", "f@example.com", false)).render
            assert(html.contains("""id="member-3""""))
            assert(html.contains("Fuqua Tarkenton"))
            assert(html.contains("""confirm('Delete Fuqua Tarkenton?') &amp;&amp; @delete('/delete-row/3')"""))

        test("the repository is seeded with the roster"):
            assert(Members.seed.map(_.id) == Seq(1L, 2L, 3L, 4L, 5L))

    end tests

end DeleteRowViewTest
