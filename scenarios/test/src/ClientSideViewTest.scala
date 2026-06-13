// PURPOSE: Unit tests for the client-only examples — custom event, bubbling, web component, etc.
// PURPOSE: These have no server; the test pins that each renders the typed client-side attributes.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ClientSideViewTest extends TestSuite:

    val tests = Tests:

        test("custom-event listens for a custom event and dispatches one"):
            val html = CustomEventView.demo.render
            assert(html.contains("""data-on:notify="$received = evt.detail.text""""))
            assert(html.contains("new CustomEvent('notify'"))
            assert(html.contains("""data-text="$received""""))

        test("event-bubbling reads the click target on a single list handler"):
            val html = EventBubblingView.demo.render
            assert(html.contains("""data-on:click="$picked = evt.target.closest('li')"""))
            assert(html.contains("""data-text="$picked""""))

        test("web-component binds a signal onto a custom element attribute"):
            val html = WebComponentView.demo.render
            assert(html.contains("customElements.define('hello-badge'"))
            assert(html.contains("""data-bind="who""""))
            assert(html.contains("""data-attr:name="$who""""))

        test("on-signal-patch reacts to patches and renders the live store"):
            val html = OnSignalPatchView.demo.render
            assert(html.contains("""data-on-signal-patch="$patches++""""))
            assert(html.contains("data-json-signals__terse"))
            assert(html.contains("""data-bind="first""""))

        test("custom-plugin invokes hand-written JavaScript from a data-on expression"):
            val html = CustomPluginView.demo.render
            assert(html.contains("window.flash"))
            assert(html.contains("""data-on:click="flash(el)""""))

    end tests

end ClientSideViewTest
