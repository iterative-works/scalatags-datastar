// PURPOSE: The shared HTML shell for scenario pages — one document head, one pinned Datastar client.
// PURPOSE: Centralising the client version keeps every example on the build the SSE codec targets.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*

/** Wraps a page's body in a complete HTML document with the standard head.
  *
  * The Datastar client is pinned here, in one place, to the release whose wire format the SSE codec
  * targets (`datastar-patch-*`); npm's "latest" tag still ships the older `merge-*` names. Every
  * scenario renders through this shell so no example can drift onto a mismatched client.
  */
object Layout:

    val datastarScript =
        "https://cdn.jsdelivr.net/gh/starfederation/datastar@v1.0.2/bundles/datastar.js"

    /** The full page as an HTML string: the standard head titled `pageTitle` (plus any `headExtra`,
      * e.g. the gallery's stylesheet and highlighter), then `content`.
      */
    def page(pageTitle: String, headExtra: Frag = frag())(content: Frag*): String =
        "<!DOCTYPE html>" + html(lang := "en")(
            head(
                meta(attr("charset") := "utf-8"),
                meta(
                    name := "viewport",
                    attr("content") := "width=device-width, initial-scale=1"
                ),
                tag("title")(pageTitle),
                script(src := datastarScript, `type` := "module"),
                headExtra
            ),
            body(content*)
        ).render

end Layout
