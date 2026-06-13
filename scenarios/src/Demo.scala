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

    val activeSearch: Demo = Demo(
        id = "active-search",
        title = "Active search",
        blurb =
            "A debounced bound input filters a contact catalogue server-side; the same contacts " +
                "fragment renders the initial cards and every SSE patch.",
        widget = ActiveSearchView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "ActiveSearch.scala", "active-search-store"),
            SnippetRef("The template", "ActiveSearchView.scala", "active-search-view"),
            SnippetRef("The endpoints", "ActiveSearchEndpoints.scala", "active-search-endpoints"),
            SnippetRef("The SSE handler", "ActiveSearchServer.scala", "active-search-server")
        )
    )

    val lazyLoad: Demo = Demo(
        id = "lazy-load",
        title = "Lazy load",
        blurb = "A placeholder carries data-init, so the reverse-routed @get fires on mount; the " +
            "server streams a fragment that replaces the placeholder by its id.",
        widget = LazyLoadView.demo,
        snippets = Seq(
            SnippetRef("The template", "LazyLoadView.scala", "lazy-load-view"),
            SnippetRef("The endpoints", "LazyLoadEndpoints.scala", "lazy-load-endpoints"),
            SnippetRef("The SSE handler", "LazyLoadServer.scala", "lazy-load-server")
        )
    )

    val lazyTabs: Demo = Demo(
        id = "lazy-tabs",
        title = "Lazy tabs",
        blurb = "Clicking a tab fires @get('/lazy-tabs/{i}') with the index as a typed Int path " +
            "parameter; the server returns the whole widget with that tab selected.",
        widget = LazyTabsView.demo,
        snippets = Seq(
            SnippetRef("The template", "LazyTabsView.scala", "lazy-tabs-view"),
            SnippetRef("The endpoints", "LazyTabsEndpoints.scala", "lazy-tabs-endpoints"),
            SnippetRef("The SSE handler", "LazyTabsServer.scala", "lazy-tabs-server")
        )
    )

    val titleUpdate: Demo = Demo(
        id = "title-update",
        title = "Title update",
        blurb = "A button's action patches the document <title> over SSE — the codec targets any " +
            "element by CSS selector, including ones in the head.",
        widget = TitleUpdateView.demo,
        snippets = Seq(
            SnippetRef("The template", "TitleUpdateView.scala", "title-update-view"),
            SnippetRef("The endpoints", "TitleUpdateEndpoints.scala", "title-update-endpoints"),
            SnippetRef("The SSE handler", "TitleUpdateServer.scala", "title-update-server")
        )
    )

    val progressBar: Demo = Demo(
        id = "progress-bar",
        title = "Progress bar",
        blurb =
            "A one-way feed: data-init opens an SSE stream and the server pushes a higher bar " +
                "state every tick, using the over-time form of datastarStream.",
        widget = ProgressBarView.demo,
        snippets = Seq(
            SnippetRef("The template", "ProgressBarView.scala", "progress-bar-view"),
            SnippetRef("The endpoints", "ProgressBarEndpoints.scala", "progress-bar-endpoints"),
            SnippetRef("The SSE handler", "ProgressBarServer.scala", "progress-bar-server")
        )
    )

    val progressiveLoad: Demo = Demo(
        id = "progressive-load",
        title = "Progressive load",
        blurb =
            "One click opens a feed that streams four page sections back in random order, each " +
                "patch filling its placeholder by id.",
        widget = ProgressiveLoadView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "ProgressiveLoad.scala", "progressive-load-store"),
            SnippetRef("The template", "ProgressiveLoadView.scala", "progressive-load-view"),
            SnippetRef(
                "The endpoints",
                "ProgressiveLoadEndpoints.scala",
                "progressive-load-endpoints"
            ),
            SnippetRef("The SSE handler", "ProgressiveLoadServer.scala", "progressive-load-server")
        )
    )

    val clickToLoad: Demo = Demo(
        id = "click-to-load",
        title = "Click to load",
        blurb = "A Load-more button appends the next page of rows and patches the offset signal " +
            "forward, so the stateless server picks up where the last click left off.",
        widget = ClickToLoadView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "ClickToLoad.scala", "click-to-load-store"),
            SnippetRef("The template", "ClickToLoadView.scala", "click-to-load-view"),
            SnippetRef("The endpoints", "ClickToLoadEndpoints.scala", "click-to-load-endpoints"),
            SnippetRef("The SSE handler", "ClickToLoadServer.scala", "click-to-load-server")
        )
    )

    val infiniteScroll: Demo = Demo(
        id = "infinite-scroll",
        title = "Infinite scroll",
        blurb = "A sentinel with data-on-intersect.once fetches the next page as it scrolls into " +
            "view; the server re-arms a fresh sentinel each time until the list is exhausted.",
        widget = InfiniteScrollView.demo,
        snippets = Seq(
            SnippetRef("The signal store", "InfiniteScroll.scala", "infinite-scroll-store"),
            SnippetRef("The template", "InfiniteScrollView.scala", "infinite-scroll-view"),
            SnippetRef(
                "The endpoints",
                "InfiniteScrollEndpoints.scala",
                "infinite-scroll-endpoints"
            ),
            SnippetRef("The SSE handler", "InfiniteScrollServer.scala", "infinite-scroll-server")
        )
    )

    val inlineValidation: Demo = Demo(
        id = "inline-validation",
        title = "Inline validation",
        blurb = "Each field validates server-side on a debounced keystroke; the handler patches " +
            "every field's error element, and submit either re-shows them or replaces the form.",
        widget = InlineValidationView.demo,
        snippets = Seq(
            SnippetRef("The store & rules", "InlineValidation.scala", "inline-validation-store"),
            SnippetRef("The template", "InlineValidationView.scala", "inline-validation-view"),
            SnippetRef(
                "The endpoints",
                "InlineValidationEndpoints.scala",
                "inline-validation-endpoints"
            ),
            SnippetRef(
                "The SSE handler",
                "InlineValidationServer.scala",
                "inline-validation-server"
            )
        )
    )

    val formData: Demo = Demo(
        id = "form-data",
        title = "Form data",
        blurb = "The submit action carries {contentType: 'form'}, so Datastar sends the form's " +
            "fields form-encoded instead of the signal store; the server reads them with formBody.",
        widget = FormDataView.demo,
        snippets = Seq(
            SnippetRef("The template", "FormDataView.scala", "form-data-view"),
            SnippetRef("The endpoints", "FormDataEndpoints.scala", "form-data-endpoints"),
            SnippetRef("The SSE handler", "FormDataServer.scala", "form-data-server")
        )
    )

    val deleteRow: Demo = Demo(
        id = "delete-row",
        title = "Delete row",
        blurb = "A table backed by a server repository; each row's Delete button guards a typed " +
            "@delete with confirm(), and the server patches the row out by id (mode=remove).",
        widget = DeleteRowView.demo,
        snippets = Seq(
            SnippetRef("The store", "DeleteRow.scala", "delete-row-store"),
            SnippetRef("The template", "DeleteRowView.scala", "delete-row-view"),
            SnippetRef("The endpoints", "DeleteRowEndpoints.scala", "delete-row-endpoints"),
            SnippetRef("The SSE handler", "DeleteRowServer.scala", "delete-row-server")
        )
    )

    val all: Seq[Demo] = Seq(
        counter,
        search,
        activeSearch,
        lazyLoad,
        lazyTabs,
        titleUpdate,
        progressBar,
        progressiveLoad,
        clickToLoad,
        infiniteScroll,
        inlineValidation,
        formData,
        deleteRow
    )

    def byId(id: String): Option[Demo] = all.find(_.id == id)

end Demos
