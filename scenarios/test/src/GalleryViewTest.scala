// PURPOSE: Unit tests for the gallery chrome — sidebar navigation, the demo box, and source panels.
// PURPOSE: Pins the active-link marking, the highlightable snippets, and the pinned client versions.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object GalleryViewTest extends TestSuite:

    private val sampleSnippet =
        Demos.counter.snippets.head -> "final case class Counter(count: Int = 0)"

    val tests = Tests:

        test("home is a complete document that loads the pinned Datastar client"):
            assert(Gallery.home.startsWith("<!DOCTYPE html>"))
            assert(Gallery.home.contains("starfederation/datastar@v1.0.2"))

        test("home lists every demo as a sidebar link"):
            assert(Gallery.home.contains("""href="/examples/counter""""))
            assert(Gallery.home.contains("""href="/examples/search""""))
            assert(Gallery.home.contains("Server-driven counter"))
            assert(Gallery.home.contains("Live search"))

        test("a demo page marks its own sidebar link active and leaves the others plain"):
            val html = Gallery.demoPage(Demos.counter, Seq.empty)
            assert(html.contains("""<a href="/examples/counter" class="active">"""))
            assert(html.contains("""<a href="/examples/search">"""))

        test("a demo page embeds the live widget"):
            val html = Gallery.demoPage(Demos.counter, Seq.empty)
            assert(html.contains("""data-on:click="@post('/increment')""""))

        test("a demo page renders each snippet as Scala under its caption"):
            val html = Gallery.demoPage(Demos.counter, Seq(sampleSnippet))
            assert(html.contains("""class="language-scala""""))
            assert(html.contains("The signal store"))
            assert(html.contains("final case class Counter(count: Int = 0)"))

        test("a demo page loads the pinned highlight.js build and the Scala grammar"):
            val html = Gallery.demoPage(Demos.counter, Seq.empty)
            assert(html.contains(s"highlightjs/cdn-release@${Gallery.highlightVersion}"))
            assert(html.contains("languages/scala.min.js"))

        test("a demo page shows the title and blurb"):
            val html = Gallery.demoPage(Demos.search, Seq.empty)
            assert(html.contains("Live search"))
            assert(html.contains(Demos.search.blurb))

    end tests

end GalleryViewTest
