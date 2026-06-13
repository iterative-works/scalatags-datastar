// PURPOSE: The bulk-update widget — a table of checkboxes and two bulk-action buttons.
// PURPOSE: Each checkbox binds an element of the selections array; the buttons @put the selection.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The bulk-update example's live fragment.
  *
  * Each row's checkbox two-way binds an element of the `selections` array
  * (`data-bind:selections[i]`, the array-index bind expressed with the string builder); the two
  * buttons fire a reverse-routed `@put` that sends the whole selection in the signal body. The
  * server flips the chosen accounts' status and re-renders the table. The table lazy-loads its rows
  * on init, so a reload reflects the live store.
  */
object BulkUpdateView:

    private val loadAction: String = BulkUpdateEndpoints.rowsRoute.action
    private val activateAction: String = BulkUpdateEndpoints.activateRoute.action
    private val deactivateAction: String = BulkUpdateEndpoints.deactivateRoute.action

    // snippet: bulk-update-view
    /** One account row, keyed `account-{id}`; its checkbox binds `selections[index]` via
      * data-bind's value form (the colon form's brackets are not a legal attribute name).
      */
    def row(account: Account, index: Int): Frag =
        tr(id := s"account-${account.id}")(
            td(input(`type` := "checkbox", dataBind := s"selections[$index]")),
            td(account.name),
            td(account.email),
            td(cls := (if account.active then "active" else "inactive"))(
                if account.active then "Active" else "Inactive"
            )
        )

    def rows(accounts: Seq[Account]): Frag =
        frag(accounts.zipWithIndex.map((account, index) => row(account, index)))

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
