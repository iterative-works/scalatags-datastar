// PURPOSE: Builds the Datastar SSE events a server streams back — patch-elements, patch-signals.
// PURPOSE: Renders each event to its exact wire format; stack-neutral, the transport lives downstream.
package works.iterative.scalatags.datastar.sse

import scalatags.Text.Frag
import scalatags.Text.all.raw
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import zio.json.*

/** The Datastar Server-Sent Events a backend streams to the browser.
  *
  * Datastar drives the page by streaming SSE events of two types — `datastar-patch-elements` to
  * patch HTML into the DOM and `datastar-patch-signals` to patch the signal store. Each method here
  * renders one event to the exact wire format Datastar parses, as a string ending in the blank line
  * that terminates an SSE event. Concatenating several gives a multi-event response.
  *
  * Only non-default options produce data lines, so the common case is compact: a bare
  * `patchElements(frag)` is just `event:` + `data: elements …`. Element and signal payloads that
  * span multiple lines are split into one `data:` line each, as the SSE protocol requires.
  */
object ServerSentEvents:

    /** Datastar's default SSE reconnection delay. A patch event only emits a `retry` line when its
      * duration differs from this, matching the reference SDKs.
      */
    val DefaultSseRetryDuration: FiniteDuration = 1000.millis

    /** Patches HTML elements into the DOM.
      *
      * @param elements
      *   the elements to patch; rendered and split across `data: elements` lines. Empty (e.g. a
      *   [[ElementPatchMode.Remove]]) emits no elements lines.
      * @param selector
      *   a CSS selector for the target; omitted for the default `outer`/`replace` targeting by id.
      * @param mode
      *   the patch strategy; [[ElementPatchMode.Outer]] (the default) emits no `mode` line.
      * @param useViewTransition
      *   whether the browser wraps the patch in a view transition; `false` emits no line.
      * @param namespace
      *   an `svg` or `mathml` namespace to create the elements in, if not HTML.
      */
    def patchElements(
        elements: Frag,
        selector: Option[String] = None,
        mode: ElementPatchMode = ElementPatchMode.Outer,
        useViewTransition: Boolean = false,
        namespace: Option[String] = None,
        eventId: Option[String] = None,
        retryDuration: FiniteDuration = DefaultSseRetryDuration
    ): String =
        val html = elements.render
        val elementLines =
            if html.isEmpty then Seq.empty
            else dataLinesFor("elements", html)
        val dataLines =
            selector.map(s => s"selector $s").toSeq ++
                Option.when(mode != ElementPatchMode.Outer)(s"mode ${mode.value}") ++
                Option.when(useViewTransition)("useViewTransition true") ++
                namespace.map(n => s"namespace $n") ++
                elementLines
        writeEvent("datastar-patch-elements", eventId, retryDuration, dataLines)
    end patchElements

    /** Patches signals into the page's signal store, serializing the model to JSON.
      *
      * @param signals
      *   the signals to merge; encoded as a compact JSON object. A field set to `null` removes that
      *   signal.
      * @param onlyIfMissing
      *   update each signal only if it does not already exist; `false` emits no line.
      */
    def patchSignals[A](
        signals: A,
        onlyIfMissing: Boolean = false,
        eventId: Option[String] = None,
        retryDuration: FiniteDuration = DefaultSseRetryDuration
    )(using JsonEncoder[A]): String =
        patchSignalsRaw(signals.toJson, onlyIfMissing, eventId, retryDuration)

    /** Patches signals from a pre-serialized JSON string, split across `data: signals` lines. The
      * typed [[patchSignals]] is the usual entry point; this is the escape hatch for callers
      * holding raw JSON.
      */
    def patchSignalsRaw(
        signals: String,
        onlyIfMissing: Boolean = false,
        eventId: Option[String] = None,
        retryDuration: FiniteDuration = DefaultSseRetryDuration
    ): String =
        val dataLines = Option.when(onlyIfMissing)("onlyIfMissing true").toSeq ++
            dataLinesFor("signals", signals)
        writeEvent("datastar-patch-signals", eventId, retryDuration, dataLines)
    end patchSignalsRaw

    /** Runs a script in the browser by appending a `<script>` element to `<body>`.
      *
      * @param script
      *   the JavaScript source; placed verbatim inside the element.
      * @param attributes
      *   attributes to set on the `<script>` element, in order.
      * @param autoRemove
      *   add `data-effect="el.remove()"` so the element removes itself after running (the default).
      */
    def executeScript(
        script: String,
        attributes: Seq[(String, String)] = Seq.empty,
        autoRemove: Boolean = true,
        eventId: Option[String] = None,
        retryDuration: FiniteDuration = DefaultSseRetryDuration
    ): String =
        val attrs =
            (if autoRemove then Seq("data-effect" -> "el.remove()") else Seq.empty) ++ attributes
        val attrStr = attrs.map((name, value) => s""" $name="$value"""").mkString
        patchElements(
            raw(s"<script$attrStr>$script</script>"),
            selector = Some("body"),
            mode = ElementPatchMode.Append,
            eventId = eventId,
            retryDuration = retryDuration
        )
    end executeScript

    /** One `data: <key> <line>` per line of `content`, split so multiline payloads stay valid SSE.
      */
    private def dataLinesFor(key: String, content: String): Seq[String] =
        content.split("\n", -1).toIndexedSeq.map(line => s"$key $line")

    /** Renders one SSE event: the `event` line, optional `id` and `retry` lines, the `data` lines,
      * and the terminating blank line. `retry` is emitted only when it differs from the default.
      */
    private def writeEvent(
        eventType: String,
        eventId: Option[String],
        retryDuration: FiniteDuration,
        dataLines: Seq[String]
    ): String =
        val idLine = eventId.fold("")(id => s"id: $id\n")
        val retryLine =
            if retryDuration != DefaultSseRetryDuration then s"retry: ${retryDuration.toMillis}\n"
            else ""
        val data = dataLines.map(line => s"data: $line\n").mkString
        s"event: $eventType\n" + idLine + retryLine + data + "\n"
    end writeEvent

end ServerSentEvents
