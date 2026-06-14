// PURPOSE: The animations example's domain — the view-transition signal and the demo's fixed data.
// PURPOSE: One ViewTransition case class seeds data-signals and decodes the round-tripped store.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.{JsonDecoder, JsonEncoder}

/** The view-transitions technique's signal store: which of the two panel states is shown. The
  * client carries it, the server reads it from the `datastar` query parameter, returns the other
  * state with a view transition, and patches the flag forward — so successive clicks alternate.
  */
// snippet: animations-store
final case class ViewTransition(swapped: Boolean = false) derives Signals, JsonEncoder, JsonDecoder
object ViewTransition extends Signals.Handles[ViewTransition]:
    val swapped = signal("swapped")
// snippet-end

/** The fixed data the animation techniques animate over. */
object Animations:

    /** The colours the throb feed cycles through; it starts and ends on the first. */
    val throbColors: Seq[String] =
        Seq("#2563eb", "#dc2626", "#16a34a", "#9333ea", "#ea580c")

    /** The items the view-transition panel reorders; a stable label per item lets the View
      * Transitions API animate the change rather than blink.
      */
    val panelItems: Seq[String] = Seq("Datastar", "Scalatags", "Tapir", "ZIO")

end Animations
