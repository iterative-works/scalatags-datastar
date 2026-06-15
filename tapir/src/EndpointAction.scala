// PURPOSE: Builds a Datastar backend-action expression (@get('/url'), …) from a Tapir endpoint.
// PURPOSE: The verb is derived from the endpoint method, so the action cannot drift from the route.
package works.iterative.scalatags.datastar.tapir

import sttp.model.Method
import sttp.tapir.PublicEndpoint
import works.iterative.scalatags.datastar.ActionOptions

/** A Datastar backend action from a typed endpoint, as the `endpoint.action` extension.
  *
  * A Datastar action like `@get('/users/42')` names a backend by verb and URL. `endpoint.action`
  * derives both from a Tapir endpoint: the URL by reverse-routing the endpoint's typed input (see
  * [[EndpointUrl]]), and the verb from the endpoint's fixed HTTP method. A reference to a missing
  * or wrong-shaped endpoint does not compile, and neither the URL nor the verb can drift from the
  * route definition.
  *
  * An endpoint with a typed input awaits its value — `toggleTodo.action(7L)` — while an input-free
  * endpoint is a complete action on its own — `increment.action`. The optional [[ActionOptions]]
  * argument appends Datastar's options object after the URL, e.g. `save.action(42L,
  * ActionOptions.form)` → `@post('/save', {contentType: 'form'})`.
  *
  * The reverse-routed URL is escaped into the action's single-quoted string literal, so input
  * values containing an apostrophe cannot break out of the expression or inject further Datastar
  * syntax.
  */
extension [I](endpoint: PublicEndpoint[I, ?, ?, Any])
    /** The complete action for an endpoint applied to its input value, e.g. `users.action(42) ==
      * "@get('/users/42')"`.
      */
    def action(input: I): String = EndpointAction.render(endpoint, ActionOptions.empty)(input)

    /** As [[action]], with Datastar's action options object appended after the URL. */
    def action(input: I, options: ActionOptions): String =
        EndpointAction.render(endpoint, options)(input)
end extension

extension (endpoint: PublicEndpoint[Unit, ?, ?, Any])
    /** The complete action for an input-free endpoint — no value to supply, e.g. `increment.action
      * \== "@post('/increment')"`.
      */
    def action: String = EndpointAction.render(endpoint, ActionOptions.empty)(())

    /** As [[action]], with Datastar's action options object appended after the URL. */
    def action(options: ActionOptions): String = EndpointAction.render(endpoint, options)(())
end extension

/** The verb derivation and URL escaping behind the `endpoint.action` extension. */
object EndpointAction:

    /** The Datastar action verb for an endpoint's HTTP method.
      *
      * The four mutating verbs map directly. Everything else — GET, an endpoint that fixes no
      * method, and methods Datastar has no action for (HEAD, OPTIONS, …) — maps to `get`. The
      * methodless default mirrors Tapir's own client interpreter, which realizes a methodless
      * endpoint with `method.getOrElse(Method.GET)`; for a non-action method, `get` is the closest
      * expressible Datastar action.
      */
    private def verb(method: Option[Method]): String = method.map(_.method) match
        case Some("POST")   => "post"
        case Some("PUT")    => "put"
        case Some("PATCH")  => "patch"
        case Some("DELETE") => "delete"
        case _              => "get"

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

    /** Builds the action expression for an endpoint, applied to its input value. */
    private[tapir] def render[I](
        endpoint: PublicEndpoint[I, ?, ?, Any],
        options: ActionOptions
    ): I => String =
        val v = verb(endpoint.method)
        val url = EndpointUrl.urlOf(endpoint)
        val opts = options.render
        val suffix = if opts.isEmpty then "" else s", $opts"
        input => s"@$v('${escapeUrl(url(input))}'$suffix)"
    end render

end EndpointAction
