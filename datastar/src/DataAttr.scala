// PURPOSE: The typed Datastar attribute value: a `data-*` name plus an ordered list of modifiers.
// PURPOSE: Capability traits give each attribute exactly the modifiers Datastar accepts for it.
package works.iterative.scalatags.datastar

import scalatags.generic
import scala.concurrent.duration.{FiniteDuration, SECONDS}

/** A Datastar attribute under construction: a base `data-*` name (already including any colon key)
  * plus the modifiers appended so far. Modifiers render as `__name` suffixes with `.`-separated
  * arguments, e.g. `data-on:click__debounce.500ms.leading`.
  *
  * Concrete subtypes expose only the modifiers Datastar accepts for that attribute, via the
  * capability traits below. `:=` binds a value exactly as on a plain `generic.Attr`, so
  * `dataOn("click").debounce(500.millis) := "@get('/x')"` produces a Scalatags `Modifier`.
  */
sealed abstract class DataAttr(val name: String, val mods: Vector[String]):

    /** The full attribute name including modifier suffixes. */
    final def attrName: String =
        if mods.isEmpty then name else name + mods.map("__" + _).mkString

    /** The Scalatags attribute this builds to. */
    final def toAttr: generic.Attr = generic.Attr(attrName)

    /** Binds a value to this attribute, yielding a Scalatags `Modifier`. */
    final def :=[Builder, T](v: T)(using
        ev: generic.AttrValue[Builder, T]
    ): generic.AttrPair[Builder, T] =
        toAttr := v

end DataAttr

/** Shared mechanism: capability traits add modifiers via `addMod`, which each concrete attribute
  * implements by reconstructing itself with the new modifier appended.
  */
trait Mods[Self <: DataAttr]:
    protected def addMod(mod: String): Self

/** `__case.camel` / `.kebab` / `.snake` / `.pascal` — controls how a key maps to a signal name. */
trait CaseMods[Self <: DataAttr] extends Mods[Self]:
    def caseCamel: Self = addMod("case.camel")
    def caseKebab: Self = addMod("case.kebab")
    def caseSnake: Self = addMod("case.snake")
    def casePascal: Self = addMod("case.pascal")
end CaseMods

/** `__debounce` / `__throttle` / `__delay` — rate-limit an event or effect. */
trait Timing[Self <: DataAttr] extends Mods[Self]:
    def debounce(
        duration: FiniteDuration,
        leading: Boolean = false,
        notrailing: Boolean = false
    ): Self =
        addMod(
            s"debounce${durSuffix(duration)}${flag(leading, "leading")}${flag(notrailing, "notrailing")}"
        )
    def throttle(
        duration: FiniteDuration,
        noleading: Boolean = false,
        trailing: Boolean = false
    ): Self =
        addMod(
            s"throttle${durSuffix(duration)}${flag(noleading, "noleading")}${flag(trailing, "trailing")}"
        )
    def delay(duration: FiniteDuration): Self =
        addMod(s"delay${durSuffix(duration)}")
end Timing

/** `__once` — run the handler at most once. */
trait OnceMod[Self <: DataAttr] extends Mods[Self]:
    def once: Self = addMod("once")

/** `__viewtransition` — wrap the resulting DOM change in a view transition. */
trait ViewTransitionMod[Self <: DataAttr] extends Mods[Self]:
    def viewTransition: Self = addMod("viewtransition")

/** `data-on:<event>` — a DOM event handler with listener options, timing and case modifiers. */
final class OnAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      Timing[OnAttr],
      CaseMods[OnAttr],
      OnceMod[OnAttr],
      ViewTransitionMod[OnAttr]:
    protected def addMod(mod: String): OnAttr = new OnAttr(name, mods :+ mod)
    def passive: OnAttr = addMod("passive")
    def capture: OnAttr = addMod("capture")
    def window: OnAttr = addMod("window")
    def document: OnAttr = addMod("document")
    def outside: OnAttr = addMod("outside")
    def prevent: OnAttr = addMod("prevent")
    def stop: OnAttr = addMod("stop")
end OnAttr

/** Attributes whose only modifiers are the case set (`data-class`, `data-computed`, etc.). */
final class CaseAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      CaseMods[CaseAttr]:
    protected def addMod(mod: String): CaseAttr = new CaseAttr(name, mods :+ mod)

/** `data-signals` — case modifiers plus `__ifmissing` (only patch signals that are absent). */
final class SignalsAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      CaseMods[SignalsAttr]:
    protected def addMod(mod: String): SignalsAttr = new SignalsAttr(name, mods :+ mod)
    def ifMissing: SignalsAttr = addMod("ifmissing")
end SignalsAttr

/** `data-bind` — case modifiers plus `__prop` (bind a DOM property) and `__event`. */
final class BindAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      CaseMods[BindAttr]:
    protected def addMod(mod: String): BindAttr = new BindAttr(name, mods :+ mod)
    def prop: BindAttr = addMod("prop")
    def event: BindAttr = addMod("event")

    /** Two-way binds a typed signal handle by its bare name (`data-bind="step"`). Unlike other
      * attributes, `data-bind` names a signal rather than taking an expression, so the handle
      * renders its name, not its `$`-prefixed reference.
      */
    def :=[Builder](signal: Signal[?])(using
        ev: generic.AttrValue[Builder, String]
    ): generic.AttrPair[Builder, String] =
        toAttr := signal.name
end BindAttr

/** `data-on-intersect` — IntersectionObserver trigger with threshold and timing modifiers. */
final class IntersectAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      Timing[IntersectAttr],
      OnceMod[IntersectAttr],
      ViewTransitionMod[IntersectAttr]:
    protected def addMod(mod: String): IntersectAttr = new IntersectAttr(name, mods :+ mod)
    def exit: IntersectAttr = addMod("exit")
    def half: IntersectAttr = addMod("half")
    def full: IntersectAttr = addMod("full")
    def threshold(value: Double): IntersectAttr = addMod(s"threshold.${number(value)}")
end IntersectAttr

/** `data-on-interval` — fires on a timer; `__duration` sets the period. */
final class IntervalAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      ViewTransitionMod[IntervalAttr]:
    protected def addMod(mod: String): IntervalAttr = new IntervalAttr(name, mods :+ mod)
    def duration(duration: FiniteDuration, leading: Boolean = false): IntervalAttr =
        addMod(s"duration${durSuffix(duration)}${flag(leading, "leading")}")
end IntervalAttr

/** `data-on-signal-patch` — runs when signals change; supports the timing modifiers. */
final class SignalPatchAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      Timing[SignalPatchAttr]:
    protected def addMod(mod: String): SignalPatchAttr = new SignalPatchAttr(name, mods :+ mod)

/** `data-init` — runs once on load; `__delay` defers it, `__viewtransition` wraps the change. */
final class InitAttr(name: String, mods: Vector[String])
    extends DataAttr(name, mods),
      ViewTransitionMod[InitAttr]:
    protected def addMod(mod: String): InitAttr = new InitAttr(name, mods :+ mod)
    def delay(duration: FiniteDuration): InitAttr = addMod(s"delay${durSuffix(duration)}")
end InitAttr

/** `data-ignore` — `__self` ignores only this element, not its descendants. */
final class IgnoreAttr(name: String, mods: Vector[String]) extends DataAttr(name, mods),
      Mods[IgnoreAttr]:
    protected def addMod(mod: String): IgnoreAttr = new IgnoreAttr(name, mods :+ mod)
    def self: IgnoreAttr = addMod("self")

/** `data-json-signals` — `__terse` omits whitespace in the rendered JSON. */
final class JsonSignalsAttr(name: String, mods: Vector[String]) extends DataAttr(name, mods),
      Mods[JsonSignalsAttr]:
    protected def addMod(mod: String): JsonSignalsAttr = new JsonSignalsAttr(name, mods :+ mod)
    def terse: JsonSignalsAttr = addMod("terse")

/** Renders a duration as Datastar's `.<n>ms` or `.<n>s` modifier argument. */
private def durSuffix(duration: FiniteDuration): String =
    if duration.unit == SECONDS then s".${duration.length}s" else s".${duration.toMillis}ms"

/** Renders an optional boolean modifier flag as `.<name>` when set, otherwise nothing. */
private def flag(enabled: Boolean, name: String): String = if enabled then s".$name" else ""

/** Renders a number without a trailing `.0`, so `1.0` becomes `1` and `0.5` stays `0.5`. */
private def number(value: Double): String =
    if value.isWhole then value.toLong.toString else value.toString
