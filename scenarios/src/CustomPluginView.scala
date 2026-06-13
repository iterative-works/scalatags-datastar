// PURPOSE: The custom-plugin widget — a Datastar expression calls hand-written JavaScript.
// PURPOSE: The boundary case: the library renders the typed trigger; the custom behaviour is plain JS.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The custom-plugin example's live fragment.
  *
  * Datastar's plugin surface is JavaScript, so a custom behaviour is defined in a script the
  * library does not type-check. What the library *does* type is the trigger that invokes it: a
  * `data-on` expression can call any function in scope, so here the click expression calls a
  * hand-written `flash` helper. This is the deliberate escape hatch — the reverse-routed, typed
  * bindings stop at the expression boundary, and beyond it is ordinary JavaScript. No server is
  * involved.
  */
object CustomPluginView:

    private val helper: String =
        """window.flash = (el) => {
          |  el.animate(
          |    [
          |      { background: '#facc15', transform: 'scale(1.2)' },
          |      { background: '#facc15', transform: 'scale(1.2)', offset: 0.25 },
          |      { background: '', transform: 'scale(1)' }
          |    ],
          |    { duration: 700, easing: 'ease-out' }
          |  );
          |};""".stripMargin

    // snippet: custom-plugin-view
    val demo: Frag =
        div(
            script(raw(helper)),
            p("The click expression below calls a custom JavaScript helper — the library renders the " +
                "typed trigger, but the behaviour itself is plain JS:"),
            button(cls := "flashable", dataOn("click") := "flash(el)")("Flash me")
        )
    // snippet-end

end CustomPluginView
