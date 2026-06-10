// PURPOSE: Reverse-routes a Tapir endpoint to the relative URL a Datastar backend action needs.
// PURPOSE: Phase 3 spike — proves endpoint inputs encode to a `/path?query` string with no base URI.
package works.iterative.scalatags.datastar.tapir

import sttp.tapir.PublicEndpoint
import sttp.tapir.client.sttp.SttpClientInterpreter

/** Reverse-routing for Datastar backend actions.
  *
  * Datastar actions reference a backend by URL, e.g. `@get('/users/42')`. Given a Tapir endpoint,
  * [[urlOf]] produces a function from the endpoint's typed input to exactly that relative URL, so a
  * template can only reference an endpoint that exists and the URL cannot drift from the route
  * definition.
  *
  * The URL is built by the sttp client interpreter with no base URI, which yields a relative
  * `/path?query` request. No HTTP backend is involved — only the request's URI is read.
  */
object EndpointUrl:

    private val interpreter = SttpClientInterpreter()

    /** A function from the endpoint's input to its relative URL (`/path?query`). */
    def urlOf[I](endpoint: PublicEndpoint[I, ?, ?, Any]): I => String =
        val toRequest = interpreter.toRequest(endpoint, None)
        input => toRequest(input).uri.toString

end EndpointUrl
