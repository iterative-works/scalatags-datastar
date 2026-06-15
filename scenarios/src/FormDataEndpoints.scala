// PURPOSE: Tapir endpoints for the form-data example — a POST that reads a form-encoded body.
// PURPOSE: The action's {contentType: 'form'} sends the form; the server decodes it with formBody.
package works.iterative.scalatags.datastar.scenarios

import sttp.capabilities.zio.ZioStreams
import sttp.tapir.*
import works.iterative.scalatags.datastar.tapir.sse.*
import zio.stream.Stream

/** The form-data example's route: [[submitRoute]] is what the button's form action reverse-routes,
  * and [[submit]] is its server realisation reading the form-encoded fields (not the signal store).
  */
object FormDataEndpoints:

    // snippet: form-data-endpoints
    /** The route the form posts to; reverse-routed with `ActionOptions.form` to
      * `@post('/form-data/submit', {contentType: 'form'})`.
      */
    val submitRoute: PublicEndpoint[Unit, Unit, Unit, Any] =
        endpoint.post.in("form-data" / "submit")

    /** The server endpoint: the form-encoded fields arrive as a `formBody`, preserving repeated
      * names (e.g. a checkbox group), and the response patches an echo of them.
      */
    val submit: PublicEndpoint[Seq[(String, String)], Unit, Stream[Throwable, Byte], ZioStreams] =
        submitRoute.in(formBody[Seq[(String, String)]]).out(datastarEvents)
    // snippet-end

end FormDataEndpoints
