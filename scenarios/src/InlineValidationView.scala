// PURPOSE: The inline-validation widget — a sign-up form that validates each field as you type.
// PURPOSE: Shared `errorElement` fragment renders the error placeholders and every SSE patch.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

import scala.concurrent.duration.DurationInt

/** The inline-validation example's live fragment.
  *
  * Each input two-way binds to its signal and, on a debounced keydown, fires a reverse-routed
  * `@post` that validates server-side; the handler patches each field's error element by id.
  * Clicking Sign up posts to the submit action, which either re-shows the errors or replaces the
  * form with a success message. [[errorElement]] renders both the empty placeholders and the
  * patches.
  */
object InlineValidationView:

    private val validateAction: String = InlineValidationEndpoints.validateRoute.action
    private val submitAction: String = InlineValidationEndpoints.submitRoute.action

    // snippet: inline-validation-view
    /** A field's error message, keyed by `errorId` so each validation patch replaces it. */
    def errorElement(errorId: String, message: String): Frag =
        p(id := errorId, cls := "error", attr("aria-live") := "polite")(message)

    private def field(labelText: String, control: Frag, errorId: String): Frag =
        div(cls := "field")(label(labelText), control, errorElement(errorId, ""))

    val demo: Frag =
        div(dataSignals := SignupForm())(
            tag("form")(id := "signup-form")(
                field(
                    "Email",
                    input(
                        `type` := "email",
                        dataBind := SignupForm.email,
                        dataOn("keydown").debounce(500.millis) := validateAction
                    ),
                    "email-error"
                ),
                field(
                    "First name",
                    input(
                        `type` := "text",
                        dataBind := SignupForm.firstName,
                        dataOn("keydown").debounce(500.millis) := validateAction
                    ),
                    "first-name-error"
                ),
                field(
                    "Last name",
                    input(
                        `type` := "text",
                        dataBind := SignupForm.lastName,
                        dataOn("keydown").debounce(500.millis) := validateAction
                    ),
                    "last-name-error"
                ),
                button(`type` := "button", dataOn("click") := submitAction)("Sign up")
            )
        )
    // snippet-end

    /** The form replaced by a confirmation once a valid submission goes through. */
    val success: Frag =
        div(id := "signup-form", cls := "success")(p("Thanks for signing up!"))

end InlineValidationView
