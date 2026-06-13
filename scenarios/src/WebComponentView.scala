// PURPOSE: The web-component widget — a signal drives a custom element's reactive attribute.
// PURPOSE: Pure client-side: data-attr binds a signal onto a custom element the browser upgrades.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*

/** The web-component example's live fragment.
  *
  * A small custom element `<hello-badge>` re-renders whenever its `name` attribute changes;
  * `data-attr:name` binds that attribute to the `$who` signal, so typing in the bound input updates
  * the component reactively. The element is defined inline; no server is involved.
  */
object WebComponentView:

    private val definition: String =
        """if (!customElements.get('hello-badge')) {
          |  customElements.define('hello-badge', class extends HTMLElement {
          |    static get observedAttributes() { return ['name']; }
          |    connectedCallback() { this.render(); }
          |    attributeChangedCallback() { this.render(); }
          |    render() { this.innerHTML = `<strong>Hi, ${this.getAttribute('name') || 'world'}!</strong>`; }
          |  });
          |}""".stripMargin

    // snippet: web-component-view
    val demo: Frag =
        div(dataSignals := "{who: 'world'}")(
            script(raw(definition)),
            p("Type a name — it drives a custom element's attribute reactively:"),
            input(`type` := "text", placeholder := "your name", dataBind := "who"),
            tag("hello-badge")(dataAttr("name") := "$who")
        )
    // snippet-end

end WebComponentView
