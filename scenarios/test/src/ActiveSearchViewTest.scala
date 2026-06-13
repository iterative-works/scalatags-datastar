// PURPOSE: Unit tests for the active-search example's pure pieces — the widget and the catalogue.
// PURPOSE: Pins the typed bindings (bound search signal, debounced @get) and the contacts fragment.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ActiveSearchViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds the search signal store"):
            assert(ActiveSearchView.demo.render.contains("""data-signals="{search: ''}""""))

        test("the input binds the search signal and fires a debounced reverse-routed get"):
            val html = ActiveSearchView.demo.render
            assert(html.contains("""data-bind="search""""))
            assert(html.contains(
                """data-on:input__debounce.200ms="@get('/active-search/search')""""
            ))

        test("contacts renders a card per match under the contact-list id"):
            val html = ActiveSearchView.contacts(Contacts.all).render
            assert(html.contains("""id="contact-list""""))
            assert(html.contains("Bernier"))

        test("contacts shows a message when there are no matches"):
            assert(ActiveSearchView.contacts(Seq.empty).render.contains("No contacts found."))

        test("Contacts.matching filters by name or email, case-insensitively"):
            assert(Contacts.matching("BERN").exists(_.lastName == "Bernier"))
            assert(Contacts.matching("example.com").size == Contacts.all.size)
            assert(Contacts.matching("zzzz").isEmpty)
            assert(Contacts.matching("").size == Contacts.all.size)

    end tests

end ActiveSearchViewTest
