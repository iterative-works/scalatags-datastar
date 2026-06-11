// PURPOSE: The gallery's catalogue — each demo is a live widget plus the source regions behind it.
// PURPOSE: One registry the sidebar, the routes and the tests share; a new example is one entry.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.Frag

/** A captioned source excerpt to show beside a demo: the `region` marked in `resource`. */
final case class SnippetRef(caption: String, resource: String, region: String)

/** One gallery example: a stable `id` (its URL slug), display text, the live `widget`, and the
  * source `snippets` that explain how it is built.
  */
final case class Demo(
    id: String,
    title: String,
    blurb: String,
    widget: Frag,
    snippets: Seq[SnippetRef]
)

/** Every example the gallery serves, in sidebar order. Adding an example is a single entry here. */
object Demos:

    val counter: Demo = Demo(
        id = "counter",
        title = "Server-driven counter",
        blurb = "A typed signal store and a reverse-routed button; the server decodes the " +
            "round-tripped store, advances it, and streams a patch-signals event back.",
        widget = CounterView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "Counter.scala", "counter-store"),
            SnippetRef("The template", "CounterView.scala", "counter-view"),
            SnippetRef("The endpoints", "CounterEndpoints.scala", "counter-endpoints"),
            SnippetRef("The SSE handler", "CounterServer.scala", "counter-server")
        )
    )

    val search: Demo = Demo(
        id = "search",
        title = "Live search",
        blurb = "A debounced bound input fires a @get action carrying the signals; the server " +
            "filters and patches the same results fragment the page first rendered.",
        widget = SearchView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "Search.scala", "search-store"),
            SnippetRef("The template", "SearchView.scala", "search-view"),
            SnippetRef("The endpoints", "SearchEndpoints.scala", "search-endpoints"),
            SnippetRef("The SSE handler", "SearchServer.scala", "search-server")
        )
    )

    val all: Seq[Demo] = Seq(counter, search)

    def byId(id: String): Option[Demo] = all.find(_.id == id)

end Demos
