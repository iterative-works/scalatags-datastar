// PURPOSE: The bulk-update widget — a table of checkboxes and two bulk-action buttons.
// PURPOSE: Each checkbox binds an element of the selections array; the buttons @put the selection.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The bulk-update example's live fragment.
  *
  * Every row's checkbox two-way binds the *same* `selections` signal (`data-bind:selections`);
  * Datastar collects a group of checkboxes sharing one signal into a boolean array, indexed by
  * their DOM order. The two buttons fire a reverse-routed `@put` that sends the whole array in the
  * signal body; the server flips the chosen accounts' status (same order) and re-renders the table.
  * The table lazy-loads its rows on init, so a reload reflects the live store.
  */
object BulkUpdateView:

    private val loadAction: String = BulkUpdateEndpoints.rowsRoute.action
    private val activateAction: String = BulkUpdateEndpoints.activateRoute.action
    private val deactivateAction: String = BulkUpdateEndpoints.deactivateRoute.action

    // snippet: bulk-update-view
    /** One account row, keyed `account-{id}`; its checkbox joins the shared `selections` array with
      * `data-bind:selections` — Datastar slots each box into the array by render order.
      */
    def row(account: Account): Frag =
        tr(id := s"account-${account.id}")(
            td(input(`type` := "checkbox", dataBind("selections") := "")),
            td(account.name),
            td(account.email),
            td(cls := (if account.active then "active" else "inactive"))(
                if account.active then "Active" else "Inactive"
            )
        )

    def rows(accounts: Seq[Account]): Frag =
        frag(accounts.map(row))

    val demo: Frag =
        div(dataSignals := "{selections: []}")(
            div(cls := "toolbar")(
                button(dataOn("click") := activateAction)("Activate selected"),
                button(dataOn("click") := deactivateAction)("Deactivate selected")
            ),
            table(
                thead(tr(th(), th("Name"), th("Email"), th("Status"))),
                tbody(id := "accounts", dataInit := loadAction)
            )
        )
    // snippet-end

end BulkUpdateView
