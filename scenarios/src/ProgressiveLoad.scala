// PURPOSE: The progressive-load example's domain — the load-disabled signal and the page sections.
// PURPOSE: One boolean signal gates the button; the sections stream in from the server out of order.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.JsonDecoder
import zio.json.JsonEncoder

/** The progressive-load page's signal store — whether the Load button has been pressed. The button
  * sets it and binds its own `disabled` attribute to it, so a load runs only once.
  */
// snippet: progressive-load-store
final case class ProgressiveLoad(loadDisabled: Boolean = false)
    derives Signals, JsonEncoder, JsonDecoder
object ProgressiveLoad extends Signals.Handles[ProgressiveLoad]:
    val loadDisabled = signal("loadDisabled")
// snippet-end

/** A page section the server streams in. */
final case class Section(id: String, title: String, body: String)

/** The four sections that load progressively, in their natural order; the server emits them
  * shuffled, so each arrives in an unpredictable sequence.
  */
object Sections:
    val all: Seq[Section] = Seq(
        Section("header", "Header", "The masthead, loaded first or last — you never know."),
        Section("article", "Article", "The main article body, several paragraphs of content."),
        Section("comments", "Comments", "A thread of reader comments, loaded on its own."),
        Section("footer", "Footer", "The page footer with links and small print.")
    )
end Sections
