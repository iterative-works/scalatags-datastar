// PURPOSE: The active-search widget — a debounced bound input over a reusable contact-cards fragment.
// PURPOSE: The same `contacts` Frag renders the initial list and every SSE patch, in one template.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import scala.concurrent.duration.DurationInt
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The active-search example's live fragment.
  *
  * Like live search, the input two-way binds to the `search` signal and fires a debounced,
  * reverse-routed `@get` action on each keystroke. [[contacts]] is the one fragment that renders
  * the card list both for the initial page and for the SSE `patch-elements` event the server
  * streams back, so the two can never diverge.
  */
object ActiveSearchView:

    /** The search action, reverse-routed from the search route: `@get('/active-search/search')`. */
    private val searchAction: String = ActiveSearchEndpoints.searchRoute.action

    // snippet: active-search-view
    /** The contact cards. Carries the `contact-list` id so a default `patch-elements` event
      * replaces it; an empty match set keeps the element and shows a message instead.
      */
    def contacts(matches: Seq[Contact]): Frag =
        val cards: Seq[Frag] =
            if matches.isEmpty then Seq(p(cls := "empty")("No contacts found."))
            else
                matches.map: contact =>
                    div(cls := "contact")(
                        strong(s"${contact.firstName} ${contact.lastName}"),
                        span(cls := "email")(contact.email)
                    )
        div(id := "contact-list")(cards)
    end contacts

    /** The interactive search: an input two-way bound to the `search` signal that fires a
      * debounced, reverse-routed `@get` action, over the catalogue rendered by [[contacts]].
      */
    val demo: Frag =
        div(dataSignals := ActiveSearch())(
            input(
                `type` := "search",
                placeholder := "Search contacts…",
                dataBind := ActiveSearch.search,
                dataOn("input").debounce(200.millis) := searchAction
            ),
            contacts(Contacts.all)
        )
    // snippet-end

end ActiveSearchView
