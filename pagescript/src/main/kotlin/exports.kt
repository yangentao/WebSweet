@file:Suppress("OPT_IN_USAGE")

import kotlinx.browser.window
import kotlinx.dom.addClass
import org.w3c.dom.HTMLFormElement

@JsExport
@JsName("evalElementAttr")
fun evalElementAttr(eid: String) {
    val e = body.query("#${eid}[${DATA.script}]") ?: return
    val sc = e.attr(DATA.script) ?: return
    if (sc.isNotEmpty()) {
        val c = e.attr(DATA.confirm)
        if (c == null || c.isEmpty() || window.confirm(c)) {
            invokeElementAttr(e, DATA.script)
        }
    }
}


@JsExport
@JsName("formOnSubmitById")
fun formOnSubmitById(eid: String) {
    val form: HTMLFormElement = body.find("#$eid") ?: return
    form.on("submit") { evt ->
        if (!form.checkValidity()) {
            evt.preventDefault()
            evt.stopPropagation()
        }
        form.addClass("was-validated")
    }
}






