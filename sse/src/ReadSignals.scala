// PURPOSE: Decodes the signal store Datastar sends back to the server into the typed signal model.
// PURPOSE: The same case class that seeds the initial `data-signals` value is what decodes here.
package works.iterative.scalatags.datastar.sse

import zio.json.*

/** Decodes the round-tripped signal store into the typed model `A`.
  *
  * Datastar appends the whole signal store to every backend request — as the `datastar` query
  * parameter for GET actions, or the JSON request body otherwise. Either way the payload is one
  * JSON object; this parses it into `A`, the same case class that seeds the initial `data-signals`
  * value, closing the round trip. Extracting the JSON string from the request (query vs body) is
  * the transport's job, not this codec's.
  *
  * @return
  *   the decoded model, or a zio-json error message describing why the JSON did not fit `A`.
  */
def readSignals[A](json: String)(using JsonDecoder[A]): Either[String, A] =
    json.fromJson[A]
