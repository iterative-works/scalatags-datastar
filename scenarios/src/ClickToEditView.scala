// PURPOSE: The click-to-edit widget — a display view that swaps to a bound edit form and back.
// PURPOSE: #demo is patched between display and form; the form's inputs bind the profile signals.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The click-to-edit example's live fragment.
  *
  * `#demo` lazy-loads the display view on init. Edit swaps it for a form whose inputs bind the
  * profile signals (seeded server-side from the record); Save `@put`s the signals, the server
  * persists them and patches the display back. Cancel re-shows the display, and Reset `@patch`es
  * the record back to the original — exercising the PATCH verb and a signal patch to reset the
  * inputs.
  */
object ClickToEditView:

    private val viewAction: String = ClickToEditEndpoints.viewRoute.action
    private val editAction: String = ClickToEditEndpoints.editRoute.action
    private val saveAction: String = ClickToEditEndpoints.saveRoute.action
    private val resetAction: String = ClickToEditEndpoints.resetRoute.action

    // snippet: click-to-edit-view
    /** The read-only display of the record, keyed `demo`, with Edit and Reset. */
    def display(profile: Profile): Frag =
        div(id := "demo")(
            p(strong(s"${profile.firstName} ${profile.lastName}")),
            p(profile.email),
            div(cls := "actions")(
                button(dataOn("click") := editAction)("Edit"),
                button(dataOn("click") := resetAction)("Reset")
            )
        )

    /** The edit form, keyed `demo`. Inputs bind the profile signals (seeded on edit); Save disables
      * itself while the request is in flight via the `_fetching` indicator.
      */
    val form: Frag =
        div(id := "demo")(
            div(cls := "field")(
                label("First name"),
                input(`type` := "text", dataBind := Profile.firstName)
            ),
            div(cls := "field")(
                label("Last name"),
                input(`type` := "text", dataBind := Profile.lastName)
            ),
            div(cls := "field")(
                label("Email"),
                input(`type` := "email", dataBind := Profile.email)
            ),
            div(cls := "actions")(
                button(
                    dataOn("click") := saveAction,
                    dataIndicator := "_fetching",
                    dataAttr("disabled") := "$_fetching"
                )("Save"),
                button(dataOn("click") := viewAction)("Cancel")
            )
        )

    val demo: Frag =
        div(cls := "click-to-edit", dataSignals := Profiles.original)(
            div(id := "demo", dataInit := viewAction)("Loading…")
        )
    // snippet-end

end ClickToEditView
