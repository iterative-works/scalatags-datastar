// PURPOSE: Unit tests for the form-data example — the form action's content type and the echo.
// PURPOSE: Pins {contentType: 'form'} on the submit action and the rendered field echo.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object FormDataViewTest extends TestSuite:

    val tests = Tests:

        test("the submit action carries the form content type"):
            val html = FormDataView.demo.render
            assert(html.contains(
                """data-on:click="@post('/form-data/submit', {contentType: 'form'})""""
            ))
            assert(html.contains("""id="myform""""))
            assert(html.contains("""name="toppings""""))

        test("formResult shows a placeholder when empty and echoes the fields otherwise"):
            assert(FormDataView.formResult(Seq.empty).render.contains("Submit the form"))
            val html = FormDataView.formResult(Seq("name" -> "Pizza", "toppings" -> "cheese")).render
            assert(html.contains("""id="form-result""""))
            assert(html.contains("name = Pizza"))
            assert(html.contains("toppings = cheese"))

    end tests

end FormDataViewTest
