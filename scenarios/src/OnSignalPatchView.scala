// PURPOSE: The on-signal-patch widget — runs an expression whenever the signal store changes.
// PURPOSE: Pure client-side: data-on-signal-patch reacts to patches; data-json-signals shows the store.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The on-signal-patch example's live fragment.
  *
  * `data-on-signal-patch` runs its expression whenever any signal changes — here it counts the
  * patches. `data-json-signals` renders the live store as JSON (the `__terse` modifier drops the
  * whitespace), so you can watch both the values and the patch count update as you edit the inputs.
  * No server is involved.
  */
object OnSignalPatchView:

    // snippet: on-signal-patch-view
    val demo: Frag =
        div(dataSignals := "{first: 1, second: 2, patches: 0}")(
            div(cls := "field")(label("first"), input(`type` := "number", dataBind := "first")),
            div(cls := "field")(label("second"), input(`type` := "number", dataBind := "second")),
            div(dataOnSignalPatch := "$patches++")(
                p("Signal patches observed: ", strong(dataText := "$patches"))
            ),
            p("Live store:"),
            pre(dataJsonSignals.terse := "")
        )
    // snippet-end

end OnSignalPatchView
