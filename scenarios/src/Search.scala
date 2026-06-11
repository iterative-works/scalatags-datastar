// PURPOSE: The live-search example's domain — the query signal store and the catalogue it filters.
// PURPOSE: One `Search` case class seeds `data-signals` and decodes the store the browser sends back.
package works.iterative.scalatags.datastar.scenarios

import works.iterative.scalatags.datastar.Signals
import zio.json.{JsonDecoder, JsonEncoder}

/** The live-search page's signal store.
  *
  * As with the counter, one case class is the single source of truth for the whole round trip:
  * `derives Signals` seeds the initial `data-signals` object, and `derives JsonEncoder,
  * JsonDecoder` lets the same type cross the SSE wire — here decoded with `readSignals` from the
  * `datastar` query parameter a `@get` action sends. The companion mixes in [[Signals.Handles]] so
  * the template binds the input to `Search.query` as a typed handle.
  */
final case class Search(query: String = "") derives Signals, JsonEncoder, JsonDecoder
object Search extends Signals.Handles[Search]:
    val query = signal("query")

/** The fixed catalogue the example filters — a handful of programming languages. */
object Languages:

    /** Every language, in display order; also the result before any query is typed. */
    val all: Seq[String] = Seq(
        "Scala",
        "Kotlin",
        "Java",
        "JavaScript",
        "TypeScript",
        "Clojure",
        "Haskell",
        "Rust",
        "Go",
        "Python",
        "Ruby",
        "Elixir",
        "Erlang",
        "OCaml",
        "Swift"
    )

    /** The languages whose name contains `query`, case-insensitively. A blank query matches all. */
    def matching(query: String): Seq[String] =
        val needle = query.trim.toLowerCase
        if needle.isEmpty then all
        else all.filter(_.toLowerCase.contains(needle))

end Languages
