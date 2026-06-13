// PURPOSE: The file-upload example's store — the base64-encoded files Datastar puts in the signals.
// PURPOSE: A data-bind file input encodes the chosen files into this array, sent in the @post body.
package works.iterative.scalatags.datastar.scenarios

import zio.json.{JsonDecoder, JsonEncoder}

/** The file-upload page's signal store: the selected files, base64-encoded by Datastar when the
  * bound file input changes. No `Signals` derivation — the array is populated by the input, not
  * seeded from the typed model — but the JSON codecs let `readSignals` decode it server-side.
  */
// snippet: file-upload-store
final case class FileUpload(upload: List[String] = Nil) derives JsonEncoder, JsonDecoder
// snippet-end
