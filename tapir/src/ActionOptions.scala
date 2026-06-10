// PURPOSE: Typed options for a Datastar backend action — the {contentType, headers} object literal.
// PURPOSE: Renders to the JS object Datastar parses as the action's second argument, or to nothing.
package works.iterative.scalatags.datastar.tapir

/** How a Datastar action encodes its request body. */
enum ContentType(val value: String):
    /** Send the signal store as JSON (Datastar's default). */
    case Json extends ContentType("json")

    /** Send the enclosing form's fields, form-encoded, instead of the signal store. */
    case Form extends ContentType("form")
end ContentType

/** Options for a Datastar backend action, rendered as the `{…}` object literal that follows the URL
  * in `@verb('/url', {…})`. Only the fields that are set are rendered, so the default (no options)
  * produces no object at all and a bare `@verb('/url')`.
  *
  * Header keys are rendered as quoted strings (header names routinely contain `-`, which is not a
  * bare JS identifier); header values and the content type are rendered as escaped single-quoted
  * string literals. Headers keep their insertion order.
  *
  * The covered keys are `contentType` and `headers`; Datastar's remaining action options
  * (`selector`, `filterSignals`, `openWhenHidden`, the `retry*` family, …) can be added as fields
  * here when needed.
  */
final case class ActionOptions(
    contentType: Option[ContentType] = None,
    headers: Seq[(String, String)] = Seq.empty
):

    /** This options object with an additional request header appended. */
    def withHeader(name: String, value: String): ActionOptions =
        copy(headers = headers :+ (name -> value))

    /** The `{…}` object literal for these options, or the empty string when nothing is set. */
    private[tapir] def render: String =
        val fields =
            contentType.map(ct => s"contentType: ${ActionOptions.jsString(ct.value)}").toList ++
                Option
                    .when(headers.nonEmpty):
                        headers
                            .map((k, v) =>
                                s"${ActionOptions.jsString(k)}: ${ActionOptions.jsString(v)}"
                            )
                            .mkString("headers: {", ", ", "}")
                    .toList
        if fields.isEmpty then "" else fields.mkString("{", ", ", "}")
    end render

end ActionOptions

object ActionOptions:

    /** No options — renders to nothing, leaving a bare `@verb('/url')`. */
    val empty: ActionOptions = ActionOptions()

    /** Options sending the enclosing form's fields instead of the signal store. */
    def form: ActionOptions = ActionOptions(contentType = Some(ContentType.Form))

    /** Options sending the signal store as JSON (Datastar's default, made explicit). */
    def json: ActionOptions = ActionOptions(contentType = Some(ContentType.Json))

    /** Renders a string as an escaped, single-quoted JS string literal. Unlike the reverse-routed
      * URL (pre-percent-encoded by the router), header keys and values are developer-supplied, so
      * both the backslash and the apostrophe must be escaped.
      */
    private def jsString(value: String): String =
        "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'"

end ActionOptions
