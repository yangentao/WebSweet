import kotlinx.browser.window
import kotlinx.dom.addClass
import org.w3c.dom.Element
import org.w3c.dom.HTMLFormElement
import org.w3c.xhr.FormData

@JsExport
@JsName("confirmById")
fun confirmById(eid: String): Boolean {
    val e = body.query("#$eid") ?: return false
    val s = e.attr(DATA.confirm)
    return s == null || s.isEmpty() || window.confirm(s)
}

//提交dialog中的表单, 参数是按钮
//<button ... onclick='client.dialogSubmit(this)'...>
@JsExport
@JsName("submit")
fun submit(e: Element) {
    val dlg = e.closest("div.modal") ?: return
    val form = dlg.query("form") as HTMLFormElement
    if (!form.checkValidity()) {
        form.addClass("was-validated")
        return
    }
    val formData = FormData(form)
    HttpPost(form.action).data(formData).requestJsonResult { jr ->
        if (jr.OK) {
            bootstrap.Modal.getInstance(dlg).hide()
            window.location.reload()
        } else {
            body.query("#dialogErrorMessage")?.textContent = jr.msg
        }
    }
}


@JsExport
@JsName("showLargeImageById")
fun showLargeImageById(eid: String): Boolean {
    val url = body.query("#$eid")?.data(DATA.srcLarge) ?: return false
    val dlg = bootDialog {
        largeSize()
        title("查看图片")
        modalBody.addClass("bg-secondary")
        container {
            div("text-center") {
                image("border border-info rounded") {
                    src = url
                }
            }
        }
    }
    dlg.show()
    return false
}


@JsExport
@JsName("showDialogById")
fun showDialogById(eid: String): Boolean {
    val e = body.query("#$eid") ?: return false
    e.attr(DATA.modal) ?: return false
    val s = e.attr(DATA.confirm)
    if (s == null || s.isEmpty() || window.confirm(s)) {
        openServerDialog(e)
    }
    return false
}

private fun openServerDialog(e: Element) {
    val attrOpen = e.attr(DATA.modal) ?: return
    val url = e.attr("href") ?: e.attr(DATA.url) ?: return
    HttpGet(url).requestText { tv ->
        val m = body.div("modal fade") { tabIndex = -1 }
        m.htmlValue = tv
        m.on("hidden.bs.modal") {
            m.remove()
        }
        bsModal(m, attrOpen == "static").show()
    }

}

@JsExport
@JsName("bootAlert")
fun bootAlert(msg: String, title: String? = "提示") {
    val dlg = BootDialog()
    dlg.modalTitle.htmlValue = title ?: ""
    dlg.modalContainer.htmlValue = msg
    dlg.showStatic()
}

@JsExport
@JsName("bootConfirm")
fun bootConfirm(msg: String, title: String?, jsblock: () -> Unit) {
    val dlg = BootDialog().apply {
        title(title)
        buttonCancel("取消")
        buttonPrimary("确定", jsblock)
        container {
            this.htmlValue = msg
        }
    }
    dlg.showStatic()
}
