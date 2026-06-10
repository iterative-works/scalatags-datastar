// PURPOSE: Scalatags attribute builders for the Datastar hypermedia framework.
// PURPOSE: Defines the core `data-*` attributes once, platform-neutrally, for JVM and JS.
package works.iterative.scalatags.datastar

import scalatags.generic

/** Datastar `data-*` attribute builders.
  *
  * Datastar attributes take two shapes:
  *   - a "plain" form whose value is an expression or object literal, e.g. `data-text="$foo"` or
  *     `data-signals="{count: 0}"`, and
  *   - a "keyed" form that names a signal, event, class or property in the attribute itself using
  *     Datastar's colon notation, e.g. `data-signals:count="0"`, `data-on:click="..."`,
  *     `data-class:active="$open"`.
  *
  * Datastar uses no custom HTML elements, so these are plain `scalatags.generic.Attr` values that
  * work unchanged on both the JVM (`scalatags.Text`) and JS (`scalatags.JsDom`) backends.
  *
  * At this stage attribute values are plain expression strings. Typed signals, a typed expression
  * DSL, and compiler-checked backend-action references are layered on in later modules without
  * changing these builders.
  */
trait DatastarAttrs:

    /** `data-<plugin>` — the plain form; the value carries the expression or object literal. */
    protected def plain(plugin: String): generic.Attr = generic.Attr(s"data-$plugin")

    /** `data-<plugin>:<key>` — the keyed form using Datastar's colon notation. */
    protected def keyed(plugin: String, key: String): generic.Attr =
        generic.Attr(s"data-$plugin:$key")

    // Reactive state
    def dataSignals: generic.Attr = plain("signals")
    def dataSignals(key: String): generic.Attr = keyed("signals", key)
    def dataComputed(name: String): generic.Attr = keyed("computed", name)
    def dataBind(signal: String): generic.Attr = keyed("bind", signal)
    def dataRef(name: String): generic.Attr = keyed("ref", name)

    // Rendering
    def dataText: generic.Attr = plain("text")
    def dataShow: generic.Attr = plain("show")
    def dataClass: generic.Attr = plain("class")
    def dataClass(name: String): generic.Attr = keyed("class", name)
    def dataAttr: generic.Attr = plain("attr")
    def dataAttr(name: String): generic.Attr = keyed("attr", name)
    def dataStyle: generic.Attr = plain("style")
    def dataStyle(prop: String): generic.Attr = keyed("style", prop)

    // Behaviour
    def dataOn(event: String): generic.Attr = keyed("on", event)
    def dataInit: generic.Attr = plain("init")
    def dataEffect: generic.Attr = plain("effect")
    def dataIndicator(name: String): generic.Attr = keyed("indicator", name)
    def dataIgnore: generic.Attr = plain("ignore")

end DatastarAttrs

/** Entry point: `import works.iterative.scalatags.datastar.Datastar.*`. */
object Datastar extends DatastarAttrs
