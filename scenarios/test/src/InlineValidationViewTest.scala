// PURPOSE: Unit tests for the inline-validation example — the bound form and the pure field rules.
// PURPOSE: Pins the debounced validate/submit actions and each rule's required/invalid/taken cases.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object InlineValidationViewTest extends TestSuite:

    val tests = Tests:

        test("the widget seeds and binds the three form signals"):
            val html = InlineValidationView.demo.render
            assert(html.contains("""data-signals="{email: '', firstName: '', lastName: ''}""""))
            assert(html.contains("""data-bind="email""""))
            assert(html.contains("""data-bind="firstName""""))
            assert(html.contains("""data-bind="lastName""""))

        test("each input validates on a debounced keydown; the button submits"):
            val html = InlineValidationView.demo.render
            assert(html.contains(
                """data-on:keydown__debounce.500ms="@post('/inline-validation/validate')""""
            ))
            assert(html.contains("""data-on:click="@post('/inline-validation/submit')""""))

        test("emailError covers the empty, malformed and already-registered cases"):
            assert(SignupValidation.emailError("").exists(_.contains("required")))
            assert(SignupValidation.emailError("nope").exists(_.contains("valid email")))
            assert(
                SignupValidation.emailError("taken@example.com").exists(_.contains("registered"))
            )
            assert(SignupValidation.emailError("fresh@example.com").isEmpty)

        test("name fields are required and a complete form validates"):
            assert(SignupValidation.firstNameError("").exists(_.contains("required")))
            assert(SignupValidation.firstNameError("Jo").isEmpty)
            assert(!SignupValidation.isValid(SignupForm()))
            assert(SignupValidation.isValid(SignupForm("fresh@example.com", "Jo", "Lee")))

    end tests

end InlineValidationViewTest
