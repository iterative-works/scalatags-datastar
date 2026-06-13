// PURPOSE: Unit tests for the last four examples — sortable, file-upload, bad-apple, dbmon views.
// PURPOSE: Pins the SortableJS wiring, the bound file input, the framed feed, and the dbmon controls.
package works.iterative.scalatags.datastar.scenarios

import utest.*

object FinalExamplesViewTest extends TestSuite:

    val tests = Tests:

        test("sortable wires SortableJS and reads the reordered DOM into a signal"):
            val html = SortableView.demo.render
            assert(html.contains("Sortable.min.js"))
            assert(html.contains("data-init"))
            assert(html.contains("new Sortable(el"))
            assert(html.contains("""data-on:reordered="$order"""))
            assert(html.contains("""data-id="one""""))

        test("file-upload binds the file input and posts to the upload action"):
            val html = FileUploadView.demo.render
            assert(html.contains("""type="file""""))
            assert(html.contains("""data-bind="upload""""))
            assert(html.contains("""data-on:click="@post('/file-upload/submit')""""))
            assert(FileUploadView.result("ok").render.contains("""id="upload-result""""))

        test("bad-apple lazy-loads the frame feed and renders a frame"):
            val html = BadAppleView.demo.render
            assert(html.contains("""id="frame""""))
            assert(html.contains("""data-init="@get('/bad-apple/play')""""))
            assert(BadAppleFrames.all.nonEmpty)
            assert(BadAppleView.frame("X").render.contains("X"))

        test("dbmon seeds the rate controls and lazy-loads the table feed"):
            val html = DbmonView.demo.render
            assert(html.contains("""data-signals="{fps: 4, mutationRate: 40}""""))
            assert(html.contains("""data-bind="fps""""))
            assert(html.contains("""data-bind="mutationRate""""))
            assert(html.contains("""data-init="@get('/dbmon/updates')""""))
            assert(DbmonView.row(Database("Cluster 1", 5, 1.5)).render.contains("Cluster 1"))

    end tests

end FinalExamplesViewTest
