// PURPOSE: Unit tests for the edit-row example — the lazy-loaded table and the read/edit row forms.
// PURPOSE: Pins the per-row Edit/Save/Cancel actions, including the @put form save.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object EditRowViewTest extends TestSuite:

    private val joe = Person(1, "Joe Smith", "joe@example.com")

    val tests = Tests:

        test("the table body lazy-loads the current rows on init"):
            val html = EditRowView.demo.render
            assert(html.contains("""id="people""""))
            assert(html.contains("""data-init="@get('/edit-row/rows')""""))

        test("a read row shows the values and an Edit action"):
            val html = EditRowView.readRow(joe).render
            assert(html.contains("""id="person-1""""))
            assert(html.contains("Joe Smith"))
            assert(html.contains("""data-on:click="@get('/edit-row/1/edit')""""))

        test("an edit row is a pre-filled form that saves form-encoded or cancels"):
            val html = EditRowView.editRow(joe).render
            assert(html.contains("""name="name""""))
            assert(html.contains("""value="Joe Smith""""))
            assert(html.contains(
                """data-on:click="@put('/edit-row/1', {contentType: 'form'})""""
            ))
            assert(html.contains("""data-on:click="@get('/edit-row/1')""""))

    end tests

end EditRowViewTest
