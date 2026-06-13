// PURPOSE: The edit-row widget — a table whose rows toggle between a read view and an inline form.
// PURPOSE: Edit/Save/Cancel each patch the one row by id; Save submits the form-encoded fields.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The edit-row example's live fragment.
  *
  * The table body lazy-loads its rows on init. Each read-only row has an Edit button that fetches
  * an inline edit form for that row (`@get('/edit-row/{id}/edit')`); the form's Save submits the
  * edited fields form-encoded (`@put('/edit-row/{id}', {contentType: 'form'})`) and Cancel restores
  * the read view. Every action patches just the one row, keyed `person-{id}`.
  */
object EditRowView:

    private val loadAction: String = EditRowEndpoints.rowsRoute.action
    private def editAction(id: Long): String = EditRowEndpoints.editRoute.action(id)
    private def saveAction(id: Long): String =
        EditRowEndpoints.saveRoute.action(id, ActionOptions.form)
    private def cancelAction(id: Long): String = EditRowEndpoints.cancelRoute.action(id)

    // snippet: edit-row-view
    /** A read-only row, keyed `person-{id}`, with an Edit button that swaps it for the form. */
    def readRow(person: Person): Frag =
        tr(id := s"person-${person.id}")(
            td(person.name),
            td(person.email),
            td(button(`type` := "button", dataOn("click") := editAction(person.id))("Edit"))
        )

    /** The same row in edit mode: an inline form, pre-filled, that Saves form-encoded or Cancels.
      */
    def editRow(person: Person): Frag =
        tr(id := s"person-${person.id}")(
            td(attr("colspan") := "3")(
                tag("form")(cls := "edit-form")(
                    input(`type` := "text", name := "name", value := person.name),
                    input(`type` := "email", name := "email", value := person.email),
                    button(`type` := "button", dataOn("click") := saveAction(person.id))("Save"),
                    button(`type` := "button", dataOn("click") := cancelAction(person.id))("Cancel")
                )
            )
        )

    def rows(people: Seq[Person]): Frag = frag(people.map(readRow))

    val demo: Frag =
        table(
            thead(tr(th("Name"), th("Email"), th())),
            tbody(id := "people", dataInit := loadAction)
        )
    // snippet-end

end EditRowView
