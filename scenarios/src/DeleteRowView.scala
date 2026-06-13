// PURPOSE: The delete-row widget — a table whose rows the server renders and removes over SSE.
// PURPOSE: The tbody lazy-loads the current rows on init, so a reload reflects the repository state.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The delete-row example's live fragment.
  *
  * The table body carries `data-init`, so on mount it fetches the current rows from the repository
  * (rather than baking a stale snapshot into the page) — a reload always shows the live state. Each
  * row's Delete button guards a reverse-routed `@delete` with a `confirm(...)`; the server removes
  * the row from the store and patches it out of the DOM by id (`mode = remove`).
  */
object DeleteRowView:

    /** Loads the current rows: `@get('/delete-row/rows')`. */
    private val loadAction: String = DeleteRowEndpoints.rowsRoute.action

    /** The delete action for a member, reverse-routed: `@delete('/delete-row/{id}')`. */
    private def deleteAction(id: Long): String = DeleteRowEndpoints.deleteRoute.action(id)

    // snippet: delete-row-view
    /** One member row, keyed `member-{id}` so the server can remove it by id. The Delete button
      * guards the reverse-routed `@delete` with a native confirm — a composed click expression.
      */
    def row(member: Member): Frag =
        tr(id := s"member-${member.id}")(
            td(member.name),
            td(member.email),
            td(if member.active then "Active" else "Inactive"),
            td(
                button(
                    cls := "danger",
                    dataOn("click") :=
                        s"confirm('Delete ${member.name}?') && ${deleteAction(member.id)}"
                )("Delete")
            )
        )

    def rows(members: Seq[Member]): Frag = frag(members.map(row))

    val demo: Frag =
        table(
            thead(tr(th("Name"), th("Email"), th("Status"), th())),
            tbody(id := "members", dataInit := loadAction)
        )
    // snippet-end

end DeleteRowView
