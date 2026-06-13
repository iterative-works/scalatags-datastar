// PURPOSE: The lazy-tabs example's domain — the static tab titles and their on-demand bodies.
// PURPOSE: Tab selection lives in the URL and the returned markup, not in a client signal store.
package works.iterative.scalatags.datastar.scenarios

/** The fixed set of tabs the example lazily loads. There is no signal store: which tab is selected
  * is encoded entirely in the markup the server returns (HATEOAS), addressed by its index in the
  * action URL.
  */
object Tabs:

    /** The tab titles, in order; the index is the tab's identity in the action URL. */
    val titles: Seq[String] = (0 to 7).map(index => s"Tab $index")

    /** Whether `index` names a tab. */
    def isDefined(index: Int): Boolean = titles.indices.contains(index)

    /** The body content for tab `index` — what the server returns when the tab is selected. */
    def body(index: Int): String =
        s"This is the lazily-loaded content of tab $index, fetched from the server only when the " +
            "tab is first opened."

end Tabs
