// PURPOSE: The file-upload widget — a bound file input and an upload button.
// PURPOSE: data-bind base64-encodes the chosen files into the signal store; @post sends them.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The file-upload example's live fragment.
  *
  * Binding a file input with `data-bind` makes Datastar base64-encode the selected files into the
  * `upload` signal array; the Upload button's reverse-routed `@post` then sends the store, and the
  * server reports what it received. Suited to small files, as the docs note.
  */
object FileUploadView:

    private val uploadAction: String = FileUploadEndpoints.submitRoute.action

    // snippet: file-upload-view
    /** The server's report of the upload, keyed `upload-result` so the patch replaces it. */
    def result(message: String): Frag = div(id := "upload-result", cls := "result")(message)

    val demo: Frag =
        div(dataSignals := "{upload: []}")(
            input(`type` := "file", attr("multiple") := "true", dataBind := "upload"),
            button(`type` := "button", dataOn("click") := uploadAction)("Upload"),
            result("No upload yet.")
        )
    // snippet-end

end FileUploadView
