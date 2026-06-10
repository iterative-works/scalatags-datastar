// PURPOSE: Smoke test verifying Datastar attribute builders render the exact wire format.
// PURPOSE: Pins down Datastar's colon notation and plain/keyed attribute forms.
package works.iterative.scalatags.datastar

import utest._

object SmokeTest extends TestSuite:
    import scalatags.Text.all._
    import Datastar._

    val tests = Tests:
        test("plain attributes render data-<plugin>"):
            assert(div(dataText := "$foo").render == """<div data-text="$foo"></div>""")
            assert(div(dataShow := "$visible").render == """<div data-show="$visible"></div>""")
            assert(
              div(dataSignals := "{count: 0}").render
                  == """<div data-signals="{count: 0}"></div>"""
            )

        test("keyed attributes render data-<plugin>:<key> with a literal colon"):
            assert(
              button(dataOn("click") := "@post('/save')").render
                  == """<button data-on:click="@post('/save')"></button>"""
            )
            assert(
              div(dataComputed("total") := "$a + $b").render
                  == """<div data-computed:total="$a + $b"></div>"""
            )
            assert(
              div(dataClass("active") := "$open").render
                  == """<div data-class:active="$open"></div>"""
            )

        test("modifiers attach with double underscores"):
            assert(
              button(dataOn("click__debounce.500ms") := "@get('/x')").render
                  == """<button data-on:click__debounce.500ms="@get('/x')"></button>"""
            )

        test("standard scalatags attrs interoperate"):
            assert(
              input(dataBind("query") := "", `type` := "search").render
                  == """<input data-bind:query="" type="search" />"""
            )
