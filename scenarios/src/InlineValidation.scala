// PURPOSE: The inline-validation example's domain — the form signal store and the field rules.
// PURPOSE: Validation is pure (field -> optional message); the handler renders the messages over SSE.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.{JsonDecoder, JsonEncoder}

/** The sign-up form's signal store — the three bound fields. One case class is the whole round
  * trip: `derives Signals` seeds `data-signals` and the JSON codecs let `readSignals` decode the
  * store from the `@post` body the validation and submit actions send.
  */
// snippet: inline-validation-store
final case class SignupForm(email: String = "", firstName: String = "", lastName: String = "")
    derives Signals, JsonEncoder, JsonDecoder
object SignupForm extends Signals.Handles[SignupForm]:
    val email = signal("email")
    val firstName = signal("firstName")
    val lastName = signal("lastName")
// snippet-end

/** The pure validation rules for the sign-up form. Each rule maps a field value to an optional
  * error message; the handler renders those messages into the form's error elements over SSE.
  */
object SignupValidation:

    /** Emails already taken — used to demonstrate a server-only rule the client cannot know. */
    val registered: Set[String] = Set("taken@example.com", "test@example.com")

    private val emailShape = """[^@\s]+@[^@\s]+\.[^@\s]+""".r

    def emailError(email: String): Option[String] =
        val value = email.trim
        if value.isEmpty then Some("Email is required.")
        else if emailShape.matches(value) == false then Some("That is not a valid email address.")
        else if registered.contains(value.toLowerCase) then
            Some("That email is already registered.")
        else None
        end if
    end emailError

    def firstNameError(firstName: String): Option[String] =
        Option.when(firstName.trim.isEmpty)("First name is required.")

    def lastNameError(lastName: String): Option[String] =
        Option.when(lastName.trim.isEmpty)("Last name is required.")

    /** Each field's error element id paired with its message (empty when the field is valid). */
    def messages(form: SignupForm): Seq[(String, String)] =
        Seq(
            "email-error" -> emailError(form.email).getOrElse(""),
            "first-name-error" -> firstNameError(form.firstName).getOrElse(""),
            "last-name-error" -> lastNameError(form.lastName).getOrElse("")
        )

    def isValid(form: SignupForm): Boolean =
        emailError(form.email).isEmpty &&
            firstNameError(form.firstName).isEmpty &&
            lastNameError(form.lastName).isEmpty

end SignupValidation
