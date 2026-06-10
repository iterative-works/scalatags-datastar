// PURPOSE: Builds a Datastar backend-action expression (@get('/url'), …) from a Tapir endpoint.
// PURPOSE: The verb is derived from the endpoint method, so the action cannot drift from the route.
package works.iterative.scalatags.datastar.tapir

import sttp.tapir.PublicEndpoint
import sttp.model.Method

/** Datastar backend actions from typed endpoints.
  *
  * A Datastar action like `@get('/users/42')` names a backend by verb and URL. [[action]] derives
  * both from a Tapir endpoint: the URL by reverse-routing the endpoint's typed input (see
  * [[EndpointUrl]]), and the verb from the endpoint's fixed HTTP method. A reference to a missing
  * or wrong-shaped endpoint does not compile, and neither the URL nor the verb can drift from the
  * route definition.
  *
  * The reverse-routed URL is escaped into the action's single-quoted string literal, so input
  * values containing an apostrophe cannot break out of the expression or inject further Datastar
  * syntax.
  */
object EndpointAction:

    /** Datastar's expression verb for an HTTP method, defined for the methods Datastar supports as
      * backend actions (GET/POST/PUT/PATCH/DELETE).
      */
    private def verb(method: Method): Option[String] = method.method match
        case "GET"    => Some("get")
        case "POST"   => Some("post")
        case "PUT"    => Some("put")
        case "PATCH"  => Some("patch")
        case "DELETE" => Some("delete")
        case _        => None

    /** Escapes the reverse-routed URL for embedding in the action's single-quoted string literal.
      *
      * The apostrophe is the only character a reverse-routed URL can carry that would close the
      * literal early (it is an RFC 3986 sub-delim, so the router leaves it raw). Every other
      * literal-breaking character — `\`, `"`, control and non-ASCII characters — is percent-encoded
      * by the router, as pinned by `EndpointUrlTest`, so escaping the apostrophe alone is
      * sufficient.
      */
    private def escapeUrl(url: String): String =
        url.replace("'", "\\'")

    /** The action-expression builder for an endpoint, e.g. `_ => "@get('/users/42')"`.
      *
      * Defined only when the endpoint fixes a Datastar-supported method; `None` otherwise (an
      * endpoint with no method, or one using a verb Datastar has no action for, such as HEAD).
      */
    def action[I](endpoint: PublicEndpoint[I, ?, ?, Any]): Option[I => String] =
        endpoint.method.flatMap(verb).map { v =>
            val url = EndpointUrl.urlOf(endpoint)
            input => s"@$v('${escapeUrl(url(input))}')"
        }

end EndpointAction
