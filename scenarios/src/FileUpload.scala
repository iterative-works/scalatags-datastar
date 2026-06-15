// PURPOSE: The file-upload example's store — the files Datastar encodes into the signals.
// PURPOSE: A data-bind file input encodes each chosen file as {name, contents, mime} in the body.
package works.iterative.scalatags.datastar.scenarios

import zio.json.JsonDecoder
import zio.json.JsonEncoder

/** The file-upload page's signal store: the selected files. When a bound file input changes,
  * Datastar encodes each file as an object — its `name`, base64 `contents`, and `mime` type — and
  * collects them into this array. No `Signals` derivation (the array is populated by the input, not
  * seeded from the typed model), but the JSON codecs let `readSignals` decode it server-side.
  */
// snippet: file-upload-store
final case class UploadedFile(name: String, contents: String, mime: String)
    derives JsonEncoder, JsonDecoder

final case class FileUpload(upload: List[UploadedFile] = Nil) derives JsonEncoder, JsonDecoder
// snippet-end
