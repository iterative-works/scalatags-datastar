// PURPOSE: Exhaustive wire-format tests for the typed Datastar attribute and modifier builders.
// PURPOSE: Pins the exact rendered output of plain/keyed forms and every modifier suffix.
package works.iterative.scalatags.datastar

import utest.*

import scala.concurrent.duration.*

object AttributesTest extends TestSuite:
    import scalatags.Text.all.*
    import Datastar.*

    /** Render a single attribute on a `div` so we can assert the exact wire form. */
    def render(m: Modifier): String = div(m).render

    val tests = Tests:

        test("plain-form attributes render data-<plugin>"):
            assert(render(dataText := "$foo") == """<div data-text="$foo"></div>""")
            assert(render(dataShow := "$visible") == """<div data-show="$visible"></div>""")
            assert(render(dataEffect := "$a = $b") == """<div data-effect="$a = $b"></div>""")
            assert(
                render(dataSignals := "{count: 0}") == """<div data-signals="{count: 0}"></div>"""
            )
            assert(render(
                dataAttr := "{disabled: $busy}"
            ) == """<div data-attr="{disabled: $busy}"></div>""")
            assert(render(dataStyle := "{color: $c}") == """<div data-style="{color: $c}"></div>""")
            assert(render(dataBind := "query") == """<div data-bind="query"></div>""")
            assert(render(
                dataClass := "{active: $open}"
            ) == """<div data-class="{active: $open}"></div>""")
            assert(render(dataComputed := "$a + $b") == """<div data-computed="$a + $b"></div>""")
            assert(render(dataRef := "el") == """<div data-ref="el"></div>""")
            assert(render(dataIndicator := "loading") == """<div data-indicator="loading"></div>""")

        test("keyed-form attributes render data-<plugin>:<key> with a literal colon"):
            assert(render(dataSignals("count") := "0") == """<div data-signals:count="0"></div>""")
            assert(render(
                dataComputed("total") := "$a + $b"
            ) == """<div data-computed:total="$a + $b"></div>""")
            assert(render(dataBind("query") := "") == """<div data-bind:query=""></div>""")
            assert(render(
                dataClass("active") := "$open"
            ) == """<div data-class:active="$open"></div>""")
            assert(render(
                dataAttr("disabled") := "$busy"
            ) == """<div data-attr:disabled="$busy"></div>""")
            assert(render(dataStyle("color") := "$c") == """<div data-style:color="$c"></div>""")
            assert(render(dataRef("el") := "") == """<div data-ref:el=""></div>""")
            assert(render(
                dataIndicator("loading") := "$busy"
            ) == """<div data-indicator:loading="$busy"></div>""")

        test("hyphenated event attributes are distinct plugins, not keyed forms"):
            assert(render(
                dataOnIntersect := "@get('/x')"
            ) == """<div data-on-intersect="@get('/x')"></div>""")
            assert(render(
                dataOnInterval := "@get('/x')"
            ) == """<div data-on-interval="@get('/x')"></div>""")
            assert(render(
                dataOnSignalPatch := "@get('/x')"
            ) == """<div data-on-signal-patch="@get('/x')"></div>""")
            assert(
                render(dataOnSignalPatchFilter := "{include: /count/}")
                    == """<div data-on-signal-patch-filter="{include: /count/}"></div>"""
            )
            assert(render(dataIgnoreMorph := "true") == """<div data-ignore-morph="true"></div>""")
            assert(
                render(dataPreserveAttr := "class") == """<div data-preserve-attr="class"></div>"""
            )
            assert(
                render(dataJsonSignals := "$store") == """<div data-json-signals="$store"></div>"""
            )

        test("data-on is keyed by the event name"):
            assert(
                render(dataOn("click") := "@post('/save')")
                    == """<div data-on:click="@post('/save')"></div>"""
            )
            assert(
                render(dataOn("keydown") := "$handled = true")
                    == """<div data-on:keydown="$handled = true"></div>"""
            )

        test("timing modifiers render with duration and boolean flags"):
            assert(dataOn("click").debounce(500.millis).attrName == "data-on:click__debounce.500ms")
            assert(
                dataOn("click").debounce(500.millis, leading = true).attrName
                    == "data-on:click__debounce.500ms.leading"
            )
            assert(
                dataOn("input").throttle(1.second, noleading = true, trailing = true).attrName
                    == "data-on:input__throttle.1s.noleading.trailing"
            )
            assert(dataOn("keydown").delay(200.millis).attrName == "data-on:keydown__delay.200ms")
            assert(dataOnSignalPatch.debounce(
                100.millis
            ).attrName == "data-on-signal-patch__debounce.100ms")

        test("event-option modifiers chain in order"):
            assert(
                dataOn("click").once.window.prevent.attrName
                    == "data-on:click__once__window__prevent"
            )
            assert(
                dataOn("scroll").passive.capture.attrName
                    == "data-on:scroll__passive__capture"
            )
            assert(dataOn("click").outside.stop.attrName == "data-on:click__outside__stop")
            assert(dataOn("click").viewTransition.attrName == "data-on:click__viewtransition")

        test("case modifiers render case.<style>"):
            assert(dataSignals("foo").caseKebab.attrName == "data-signals:foo__case.kebab")
            assert(dataBind("myValue").caseCamel.attrName == "data-bind:myValue__case.camel")
            assert(dataClass("x").caseSnake.attrName == "data-class:x__case.snake")
            assert(dataComputed("y").casePascal.attrName == "data-computed:y__case.pascal")

        test("attribute-specific modifiers"):
            assert(dataSignals.ifMissing.attrName == "data-signals__ifmissing")
            assert(dataBind("q").prop.event.attrName == "data-bind:q__prop__event")
            assert(dataIgnore.self.attrName == "data-ignore__self")
            assert(dataJsonSignals.terse.attrName == "data-json-signals__terse")
            assert(dataInit.delay(
                500.millis
            ).viewTransition.attrName == "data-init__delay.500ms__viewtransition")

        test("intersect modifiers"):
            assert(dataOnIntersect.once.full.attrName == "data-on-intersect__once__full")
            assert(dataOnIntersect.half.exit.attrName == "data-on-intersect__half__exit")
            assert(dataOnIntersect.threshold(0.5).attrName == "data-on-intersect__threshold.0.5")
            assert(dataOnIntersect.threshold(1.0).attrName == "data-on-intersect__threshold.1")
            assert(
                dataOnIntersect.once.debounce(200.millis).attrName
                    == "data-on-intersect__once__debounce.200ms"
            )

        test("interval duration modifier"):
            assert(dataOnInterval.duration(1.second).attrName == "data-on-interval__duration.1s")
            assert(
                dataOnInterval.duration(1.second, leading = true).attrName
                    == "data-on-interval__duration.1s.leading"
            )

        test("modifiers survive rendering to a real attribute"):
            assert(
                render(dataOn("click").debounce(500.millis).once := "@get('/x')")
                    == """<div data-on:click__debounce.500ms__once="@get('/x')"></div>"""
            )
            assert(
                render(dataSignals("count").ifMissing := "0")
                    == """<div data-signals:count__ifmissing="0"></div>"""
            )
end AttributesTest
