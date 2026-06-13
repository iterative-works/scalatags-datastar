// PURPOSE: The custom-event widget — a button dispatches a bubbling custom event a listener catches.
// PURPOSE: Pure client-side: data-on:<name> listens for any DOM event, including custom ones.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The custom-event example's live fragment.
  *
  * `data-on` listens for any DOM event by name, so a hand-dispatched `CustomEvent('notify', …)` is
  * handled just like a native one. The button dispatches the event; it bubbles to the container,
  * whose `data-on:notify` copies the event detail into a signal shown live. No server is involved.
  */
object CustomEventView:

    // snippet: custom-event-view
    val demo: Frag =
        div(
            dataSignals := "{received: '(nothing yet)'}",
            dataOn("notify") := "$received = evt.detail.text"
        )(
            p("Dispatch a custom 'notify' event that bubbles up to this container:"),
            button(
                dataOn("click") :=
                    "evt.target.dispatchEvent(new CustomEvent('notify', " +
                        "{bubbles: true, detail: {text: 'Hello from a custom event!'}}))"
            )("Fire custom event"),
            p("Received: ", strong(dataText := "$received"))
        )
    // snippet-end

end CustomEventView
