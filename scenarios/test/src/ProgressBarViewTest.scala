// PURPOSE: Unit tests for the progress-bar example — the placeholder, the bar states, the step set.
// PURPOSE: The timed SSE feed itself is left to the routes test (headers only); here the pure pieces.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object ProgressBarViewTest extends TestSuite:

    val tests = Tests:

        test("the placeholder opens the reverse-routed updates feed on init"):
            val html = ProgressBarView.demo.render
            assert(html.contains("""id="progress-bar""""))
            assert(html.contains("""data-init="@get('/progress-bar/updates')""""))
            assert(html.contains("0%"))

        test("a bar renders its percentage as width and label, under the progress-bar id"):
            val html = ProgressBarView.bar(50).render
            assert(html.contains("""id="progress-bar""""))
            assert(html.contains("width:50%"))
            assert(html.contains("50%"))
            assert(ProgressBarView.bar(100).render.contains("100%"))

        test("the terminal state offers to re-run the feed"):
            val html = ProgressBarView.completed.render
            assert(html.contains("Try again"))
            assert(html.contains("""data-on:click="@get('/progress-bar/updates')""""))

        test("the streamed steps cover 0 to 100 in tens"):
            assert(ProgressBarServer.steps == (0 to 100 by 10))

    end tests

end ProgressBarViewTest
