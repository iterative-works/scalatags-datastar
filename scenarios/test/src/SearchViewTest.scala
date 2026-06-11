// PURPOSE: Unit tests for the live-search example's pure pieces — the filter and the page render.
// PURPOSE: Pins the reusable results fragment and the typed Datastar attributes the round trip needs.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object SearchViewTest extends TestSuite:

    val tests = Tests:

        test("matching filters the catalog case-insensitively"):
            assert(Languages.matching("sca") == Seq("Scala"))
            assert(Languages.matching("JAVA").contains("Java"))

        test("matching returns the whole catalog for a blank query"):
            assert(Languages.matching("") == Languages.all)
            assert(Languages.matching("   ") == Languages.all)

        test("matching returns nothing when no language matches"):
            assert(Languages.matching("zzz").isEmpty)

        test("results renders one list item per match under a stable id"):
            assert(SearchView.results(Seq("Scala")).render == """<ul id="results"><li>Scala</li></ul>""")

        test("results keeps its id but shows a message when nothing matches"):
            val html = SearchView.results(Seq.empty).render
            assert(html.startsWith("""<ul id="results">"""))
            assert(html.contains("No matches"))

        test("page seeds the signal store from the case class"):
            assert(SearchView.page.contains("""data-signals="{query: ''}""""))

        test("page two-way binds the input to the query signal"):
            assert(SearchView.page.contains("""data-bind="query""""))

        test("page debounces the input and reverse-routes the search action"):
            assert(SearchView.page.contains(
                """data-on:input__debounce.300ms="@get('/search/results')""""
            ))

        test("page renders the full catalog before any filtering"):
            assert(SearchView.page.contains("""<ul id="results">"""))
            assert(SearchView.page.contains("<li>Scala</li>"))

        test("page loads the Datastar client matching the codec's wire format"):
            assert(SearchView.page.contains("starfederation/datastar@v1.0.2"))
            assert(!SearchView.page.contains("merge-elements"))

        test("page is a complete HTML document"):
            assert(SearchView.page.startsWith("<!DOCTYPE html>"))

    end tests

end SearchViewTest
