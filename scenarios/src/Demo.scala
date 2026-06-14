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
            "parameter; the server returns the whole widget with that tab selected. (Datastar always " +
            "appends the signal store to a GET as ?datastar=… — there are no signals here, so it is {}.)",
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

    val editRow: Demo = Demo(
        id = "edit-row",
        title = "Edit row",
        blurb =
            "Each table row toggles between a read view and an inline edit form; Save submits the " +
                "fields form-encoded via @put and the server patches just that row back.",
        widget = EditRowView.demo,
        snippets = Seq(
            SnippetRef("The store", "EditRow.scala", "edit-row-store"),
            SnippetRef("The template", "EditRowView.scala", "edit-row-view"),
            SnippetRef("The endpoints", "EditRowEndpoints.scala", "edit-row-endpoints"),
            SnippetRef("The SSE handler", "EditRowServer.scala", "edit-row-server")
        )
    )

    val bulkUpdate: Demo = Demo(
        id = "bulk-update",
        title = "Bulk update",
        blurb = "Row checkboxes bind a selections array; a bulk @put sends the selection and the " +
            "server flips the chosen accounts' status, then re-renders the table.",
        widget = BulkUpdateView.demo,
        snippets = Seq(
            SnippetRef("The store", "BulkUpdate.scala", "bulk-update-store"),
            SnippetRef("The template", "BulkUpdateView.scala", "bulk-update-view"),
            SnippetRef("The endpoints", "BulkUpdateEndpoints.scala", "bulk-update-endpoints"),
            SnippetRef("The SSE handler", "BulkUpdateServer.scala", "bulk-update-server")
        )
    )

    val todomvc: Demo = Demo(
        id = "todomvc",
        title = "TodoMVC",
        blurb = "The canonical TodoMVC: add/toggle/delete/toggle-all/clear over a server todo " +
            "repository, with client filter signals; each action patches the list and count granularly.",
        widget = TodoMvcView.demo,
        snippets = Seq(
            SnippetRef("The store", "TodoMvc.scala", "todomvc-store"),
            SnippetRef("The template", "TodoMvcView.scala", "todomvc-view"),
            SnippetRef("The endpoints", "TodoMvcEndpoints.scala", "todomvc-endpoints"),
            SnippetRef("The SSE handlers", "TodoMvcServer.scala", "todomvc-server")
        )
    )

    val clickToEdit: Demo = Demo(
        id = "click-to-edit",
        title = "Click to edit",
        blurb =
            "A single record swaps between a display view and a bound edit form; Save @puts the " +
                "signals and Reset @patches back to the original — both patch the server's record.",
        widget = ClickToEditView.demo,
        snippets = Seq(
            SnippetRef("The store", "ClickToEdit.scala", "click-to-edit-store"),
            SnippetRef("The template", "ClickToEditView.scala", "click-to-edit-view"),
            SnippetRef("The endpoints", "ClickToEditEndpoints.scala", "click-to-edit-endpoints"),
            SnippetRef("The SSE handlers", "ClickToEditServer.scala", "click-to-edit-server")
        )
    )

    val svgMorphing: Demo = Demo(
        id = "svg-morphing",
        title = "SVG morphing",
        blurb = "A button morphs an inline SVG circle to a random colour and radius; the server " +
            "patches the element with the svg namespace so it is created correctly.",
        widget = SvgMorphingView.demo,
        snippets = Seq(
            SnippetRef("The template", "SvgMorphingView.scala", "svg-morphing-view"),
            SnippetRef("The endpoints", "SvgMorphingEndpoints.scala", "svg-morphing-endpoints"),
            SnippetRef("The SSE handler", "SvgMorphingServer.scala", "svg-morphing-server")
        )
    )

    val templCounter: Demo = Demo(
        id = "templ-counter",
        title = "Templ counter",
        blurb = "A counter held on the server and shared by every visitor — open two tabs and watch " +
            "both advance. Per-user counters need a session, which is outside the binding's scope.",
        widget = TemplCounterView.demo,
        snippets = Seq(
            SnippetRef("The store", "TemplCounter.scala", "templ-counter-store"),
            SnippetRef("The template", "TemplCounterView.scala", "templ-counter-view"),
            SnippetRef("The endpoints", "TemplCounterEndpoints.scala", "templ-counter-endpoints"),
            SnippetRef("The SSE handlers", "TemplCounterServer.scala", "templ-counter-server")
        )
    )

    val customEvent: Demo = Demo(
        id = "custom-event",
        title = "Custom event",
        blurb = "A button dispatches a bubbling CustomEvent that a container's data-on:notify " +
            "catches — data-on listens for any DOM event, custom ones included. Client-only.",
        widget = CustomEventView.demo,
        snippets = Seq(SnippetRef("The template", "CustomEventView.scala", "custom-event-view"))
    )

    val eventBubbling: Demo = Demo(
        id = "event-bubbling",
        title = "Event bubbling",
        blurb = "One data-on:click on the list reads evt.target to tell which item was clicked, " +
            "instead of a handler per item. Client-only.",
        widget = EventBubblingView.demo,
        snippets = Seq(SnippetRef("The template", "EventBubblingView.scala", "event-bubbling-view"))
    )

    val webComponent: Demo = Demo(
        id = "web-component",
        title = "Web component",
        blurb =
            "data-attr binds a signal onto a custom element's attribute, so a bound input drives " +
                "the component reactively. Client-only (the element is defined inline).",
        widget = WebComponentView.demo,
        snippets = Seq(SnippetRef("The template", "WebComponentView.scala", "web-component-view"))
    )

    val onSignalPatch: Demo = Demo(
        id = "on-signal-patch",
        title = "On signal patch",
        blurb = "data-on-signal-patch runs an expression whenever the store changes; " +
            "data-json-signals__terse shows the live store. Client-only.",
        widget = OnSignalPatchView.demo,
        snippets =
            Seq(SnippetRef("The template", "OnSignalPatchView.scala", "on-signal-patch-view"))
    )

    val customPlugin: Demo = Demo(
        id = "custom-plugin",
        title = "Custom plugin",
        blurb =
            "A data-on expression calls hand-written JavaScript — the typed bindings render the " +
                "trigger, but the custom behaviour is plain JS beyond the boundary. Client-only.",
        widget = CustomPluginView.demo,
        snippets = Seq(SnippetRef("The template", "CustomPluginView.scala", "custom-plugin-view"))
    )

    val sortable: Demo = Demo(
        id = "sortable",
        title = "Sortable",
        blurb = "data-init wires SortableJS to make the list draggable and dispatch a reordered " +
            "event; data-on:reordered reads the new order into a signal. Client-only.",
        widget = SortableView.demo,
        snippets = Seq(SnippetRef("The template", "SortableView.scala", "sortable-view"))
    )

    val fileUpload: Demo = Demo(
        id = "file-upload",
        title = "File upload",
        blurb =
            "data-bind base64-encodes the chosen files into the signal store; the Upload button " +
                "@posts them and the server reports the count and size.",
        widget = FileUploadView.demo,
        snippets = Seq(
            SnippetRef("The store", "FileUpload.scala", "file-upload-store"),
            SnippetRef("The template", "FileUploadView.scala", "file-upload-view"),
            SnippetRef("The endpoints", "FileUploadEndpoints.scala", "file-upload-endpoints"),
            SnippetRef("The SSE handler", "FileUploadServer.scala", "file-upload-server")
        )
    )

    val badApple: Demo = Demo(
        id = "bad-apple",
        title = "Bad apple",
        blurb = "A one-way feed plays a tiny ASCII animation, one patch-elements per frame — the " +
            "same server-driven video pattern the original uses, scaled down.",
        widget = BadAppleView.demo,
        snippets = Seq(
            SnippetRef("The frames", "BadApple.scala", "bad-apple-frames"),
            SnippetRef("The template", "BadAppleView.scala", "bad-apple-view"),
            SnippetRef("The endpoints", "BadAppleEndpoints.scala", "bad-apple-endpoints"),
            SnippetRef("The SSE handler", "BadAppleServer.scala", "bad-apple-server")
        )
    )

    val dbmon: Demo = Demo(
        id = "dbmon",
        title = "DBmon",
        blurb =
            "A benchmark: the server streams rapid table updates at a client-controlled frame " +
                "rate, re-randomising a fraction of the rows each frame.",
        widget = DbmonView.demo,
        snippets = Seq(
            SnippetRef("The store", "Dbmon.scala", "dbmon-store"),
            SnippetRef("The template", "DbmonView.scala", "dbmon-view"),
            SnippetRef("The endpoints", "DbmonEndpoints.scala", "dbmon-endpoints"),
            SnippetRef("The SSE handler", "DbmonServer.scala", "dbmon-server")
        )
    )

    val animations: Demo = Demo(
        id = "animations",
        title = "Animations",
        blurb =
            "Four techniques driven by only HTML, CSS and SSE: a colour throb over a stable id, " +
                "a view-transition swap, fade-out-then-remove, and fade-in-on-append.",
        widget = AnimationsView.demo,
        snippets = Seq(
            SnippetRef("The store", "Animations.scala", "animations-store"),
            SnippetRef("The template", "AnimationsView.scala", "animations-view"),
            SnippetRef("The endpoints", "AnimationsEndpoints.scala", "animations-endpoints"),
            SnippetRef("The SSE handlers", "AnimationsServer.scala", "animations-server")
        )
    )

    val all: Seq[Demo] = Seq(
        counter,
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
        deleteRow,
        editRow,
        bulkUpdate,
        todomvc,
        clickToEdit,
        svgMorphing,
        templCounter,
        customEvent,
        eventBubbling,
        webComponent,
        onSignalPatch,
        customPlugin,
        sortable,
        fileUpload,
        badApple,
        dbmon,
        animations
    )

    def byId(id: String): Option[Demo] = all.find(_.id == id)

end Demos
