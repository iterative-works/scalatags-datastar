// PURPOSE: The form-data widget — a form submitted as form-encoded fields via {contentType: 'form'}.
// PURPOSE: Shows the other request channel: the action sends the form, not the signal store JSON.
package works.iterative.scalatags.datastar.scenarios

import scalatags.Text.all.*
import works.iterative.scalatags.datastar.Datastar.*
import works.iterative.scalatags.datastar.tapir.*

/** The form-data example's live fragment.
  *
  * The Submit button's action carries `ActionOptions.form`, so Datastar serialises the enclosing
  * form's fields as form-encoded data — the signal store is not sent. The server reads those fields
  * with a Tapir `formBody` and patches an echo of what it received. [[formResult]] renders both the
  * empty placeholder and the patched echo.
  */
object FormDataView:

    /** The submit action with the form content type: `@post('/form-data/submit', {contentType:
      * 'form'})`.
      */
    private val submitAction: String = FormDataEndpoints.submitRoute.action(ActionOptions.form)

    private def topping(choice: String): Frag =
        label(cls := "topping")(
            input(`type` := "checkbox", name := "toppings", value := choice),
            choice
        )

    // snippet: form-data-view
    /** The server's echo of the submitted fields, keyed `form-result` so the patch replaces it. */
    def formResult(fields: Seq[(String, String)]): Frag =
        div(id := "form-result", cls := "result")(
            if fields.isEmpty then
                p(cls := "muted")("Submit the form to see what the server receives.")
            else frag(h4("The server received:"), ul(fields.map((k, v) => li(s"$k = $v"))))
        )

    val demo: Frag =
        div(
            tag("form")(id := "myform")(
                div(cls := "field")(label("Name"), input(`type` := "text", name := "name")),
                tag("fieldset")(
                    tag("legend")("Toppings"),
                    topping("cheese"),
                    topping("mushroom"),
                    topping("onion")
                ),
                button(`type` := "button", dataOn("click") := submitAction)("Submit")
            ),
            formResult(Seq.empty)
        )
    // snippet-end

end FormDataView
