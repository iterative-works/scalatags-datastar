// PURPOSE: Unit tests for the click-to-edit example — the display view and the bound edit form.
// PURPOSE: Pins the edit/reset/save/cancel actions (incl. the @patch reset) and the indicator wiring.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ClickToEditViewTest extends TestSuite:

    val tests = Tests:

        test("the display shows the record with Edit and Reset actions"):
            val html = ClickToEditView.display(Profile("Jane", "Doe", "jane@example.com")).render
            assert(html.contains("Jane Doe"))
            assert(html.contains("jane@example.com"))
            assert(html.contains("""data-on:click="@get('/click-to-edit/edit')""""))
            assert(html.contains("""data-on:click="@patch('/click-to-edit/reset')""""))

        test("the form binds the three signals and wires save, cancel and the indicator"):
            val html = ClickToEditView.form.render
            assert(html.contains("""data-bind="firstName""""))
            assert(html.contains("""data-bind="lastName""""))
            assert(html.contains("""data-bind="email""""))
            assert(html.contains("""data-on:click="@put('/click-to-edit/save')""""))
            assert(html.contains("""data-on:click="@get('/click-to-edit/view')""""))
            assert(html.contains("""data-indicator="_fetching""""))
            assert(html.contains("""data-attr:disabled="$_fetching""""))

        test("the widget seeds the signals from the original and lazy-loads the display"):
            val html = ClickToEditView.demo.render
            assert(html.contains(
                """data-signals="{firstName: 'Joe', lastName: 'Smith', email: 'joe@example.com'}""""
            ))
            assert(html.contains("""data-init="@get('/click-to-edit/view')""""))

    end tests

end ClickToEditViewTest
