// PURPOSE: Tapir endpoints for the inline-validation example — the validate and submit POST actions.
// PURPOSE: Both carry the form's signal store in the @post body, decoded by SignalsInput.body.
package works.iterative.scalatags.datastar.scenarios

import sttp.tapir.*
import sttp.capabilities.zio.ZioStreams
import zio.stream.Stream
import works.iterative.scalatags.datastar.tapir.sse.*

/** The inline-validation example's routes: [[validateRoute]] runs on each keystroke and
  * [[submitRoute]] on the button; their server forms decode the form's signal store from the body.
  */
object InlineValidationEndpoints:

    // snippet: inline-validation-endpoints
    /** The per-keystroke validation route; reverse-routes to
      * `@post('/inline-validation/validate')`.
      */
    val validateRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("inline-validation" / "validate")

    /** The submit route; reverse-routes to `@post('/inline-validation/submit')`. */
    val submitRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("inline-validation" / "submit")

    val validate: PublicEndpoint[SignupForm, Unit, Stream[Throwable, Byte], ZioStreams] =
        validateRoute.in(SignalsInput.body[SignupForm]).out(datastarEvents)

    val submit: PublicEndpoint[SignupForm, Unit, Stream[Throwable, Byte], ZioStreams] =
        submitRoute.in(SignalsInput.body[SignupForm]).out(datastarEvents)
    // snippet-end

end InlineValidationEndpoints
