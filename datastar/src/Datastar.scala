// PURPOSE: Named builders for the full standard Datastar `data-*` attribute surface.
// PURPOSE: Each builder returns a typed attribute exposing exactly the modifiers Datastar accepts.
package works.iterative.scalatags.datastar

import scalatags.generic

/** Builders for Datastar's standard `data-*` attributes.
  *
  * Attributes take two shapes:
  *   - a "plain" form whose value carries the expression or object literal, e.g.
  *     `data-text="$foo"`, built by the no-argument builder (`dataText`); and
  *   - a "keyed" form naming a signal, event, class or property in the attribute itself using
  *     Datastar's colon notation, e.g. `data-on:click`, `data-class:active`, built by the
  *     single-argument builder (`dataOn("click")`).
  *
  * Attributes that accept no modifiers are plain `generic.Attr`s; the rest return a typed
  * [[DataAttr]] subtype exposing only their valid modifiers. Datastar uses no custom HTML elements,
  * so every builder works unchanged on both the JVM (`scalatags.Text`) and JS (`scalatags.JsDom`).
  *
  * Pro (paid-tier) attributes such as `data-persist`, `data-scroll-into-view`,
  * `data-view-transition` and `data-query-string` are not included here.
  */
trait DatastarAttrs:

    private def attr(name: String): generic.Attr = generic.Attr(name)

    // --- Reactive state ---
    def dataSignals: SignalsAttr = new SignalsAttr("data-signals", Vector.empty)
    def dataSignals(key: String): SignalsAttr = new SignalsAttr(s"data-signals:$key", Vector.empty)
    def dataComputed: CaseAttr = new CaseAttr("data-computed", Vector.empty)
    def dataComputed(name: String): CaseAttr = new CaseAttr(s"data-computed:$name", Vector.empty)
    def dataBind: BindAttr = new BindAttr("data-bind", Vector.empty)
    def dataBind(signal: String): BindAttr = new BindAttr(s"data-bind:$signal", Vector.empty)
    def dataRef: CaseAttr = new CaseAttr("data-ref", Vector.empty)
    def dataRef(name: String): CaseAttr = new CaseAttr(s"data-ref:$name", Vector.empty)
    def dataJsonSignals: JsonSignalsAttr = new JsonSignalsAttr("data-json-signals", Vector.empty)

    // --- Rendering ---
    def dataText: generic.Attr = attr("data-text")
    def dataShow: generic.Attr = attr("data-show")
    def dataClass: CaseAttr = new CaseAttr("data-class", Vector.empty)
    def dataClass(name: String): CaseAttr = new CaseAttr(s"data-class:$name", Vector.empty)
    def dataAttr: generic.Attr = attr("data-attr")
    def dataAttr(name: String): generic.Attr = attr(s"data-attr:$name")
    def dataStyle: generic.Attr = attr("data-style")
    def dataStyle(prop: String): generic.Attr = attr(s"data-style:$prop")
    def dataIndicator: CaseAttr = new CaseAttr("data-indicator", Vector.empty)
    def dataIndicator(name: String): CaseAttr = new CaseAttr(s"data-indicator:$name", Vector.empty)

    // --- Behaviour ---
    def dataOn(event: String): OnAttr = new OnAttr(s"data-on:$event", Vector.empty)
    def dataOnIntersect: IntersectAttr = new IntersectAttr("data-on-intersect", Vector.empty)
    def dataOnInterval: IntervalAttr = new IntervalAttr("data-on-interval", Vector.empty)
    def dataOnSignalPatch: SignalPatchAttr =
        new SignalPatchAttr("data-on-signal-patch", Vector.empty)
    def dataOnSignalPatchFilter: generic.Attr = attr("data-on-signal-patch-filter")
    def dataInit: InitAttr = new InitAttr("data-init", Vector.empty)
    def dataEffect: generic.Attr = attr("data-effect")
    def dataIgnore: IgnoreAttr = new IgnoreAttr("data-ignore", Vector.empty)
    def dataIgnoreMorph: generic.Attr = attr("data-ignore-morph")
    def dataPreserveAttr: generic.Attr = attr("data-preserve-attr")

end DatastarAttrs

/** The core entry point: a single `import works.iterative.scalatags.datastar.Datastar.*` brings the
  * whole DSL into scope — the `data-*` attribute builders (inherited from [[DatastarAttrs]]), the
  * typed expression DSL (`lit` and the operators), the signal model (`Signal`, `Signals`), and the
  * action options object (`ActionOptions`, `ContentType`).
  */
object Datastar extends DatastarAttrs:
    export Expr.*
    export works.iterative.scalatags.datastar.{
        Expr,
        ExprLiteral,
        Signal,
        Signals,
        ActionOptions,
        ContentType
    }
end Datastar
