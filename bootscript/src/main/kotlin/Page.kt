@file:Suppress("EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")

import dialog.BootDialog
import kotlinx.browser.window
import kotlinx.dom.clear
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import kotlin.js.Json


private const val QUERY_FORM = "queryForm"

//将两个select做关联
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
@JsExport
@JsName("selectOptionsDependOn")
fun selectOptionsDependOn(selId: String, dependId: String) {
    val sel = findElementById(selId) ?: return
    val dep = findElementById(dependId) as HTMLSelectElement
    val url = sel.attr("depend_url") ?: return
    val onval = sel.attr("onval") ?: return

    val inQueryForm = sel.closest("form")?.hasAttribute(QUERY_FORM) ?: false

    dep.on("change") {
        val depSelVal = dep.value //dep["value"] ?: ""
        console.log("SelectValue: ")
        console.log(depSelVal)
        HttpGet(url).jsonResponse().param(onval, depSelVal).onSuccess {
            sel.clear()
            if (inQueryForm) {
                sel.option {
                    attr("value", "")
                    text("全部")
                }
            }
            val yv = it.jsonValue
            val data = yv["data"] as Array<*>
            for (item in data) {
                item as Json
                sel.option {
                    this.attr("value", item["key"]!!.toString())
                    text(item["label"]!!.toString())
                }
            }
        }.request()
    }

}

@JsExport
@JsName("refClear")
fun refClear(e: Element): dynamic {
    e.closest("div")?.apply {
        query(".yet-ref-key")?.attr("value", "")
        query(".yet-ref-label")?.attr("value", "")
    }
    return false
}

@JsExport
@JsName("refQuery")
@ExperimentalJsExport
fun refQuery(refBtn: HTMLElement): dynamic {
    RefQueryDialog(refBtn).show()
    return false
}

class RefQueryDialog(private val refBtn: HTMLElement) {
    private val dlg = BootDialog()
    private lateinit var inputEle: HTMLInputElement
    private lateinit var divFlex: HTMLDivElement

    init {
        dlg.apply {
            modalHeader.remove()
            container {
                div("row") {
                    div("col-9") {
                        input("form-control mb-3") {
                            type = "text"
                            placeholder = "输入内容,回车查找"
                            inputEle = this
                            on("change") {
                                dialogSelectLoadData()
                            }
                        }
                    }
                    div("col-auto ms-auto") {
                        button("btn btn-small btn-secondary") {
                            text("查找")
                            onclick = {
                                dialogSelectLoadData()
                            }
                        }
                    }
                }
                div("row") {
                    div("d-flex flex-row flex-wrap fs-5") {
                        divFlex = this
                    }
                }
            }
        }

    }

    fun show() {
        dlg.showStatic()
        dialogSelectLoadData()
    }

    private fun dialogSelectLoadData() {
        val url: String = refBtn.attr("href") ?: refBtn.attr(DATA.url) ?: return
        val searchParam = refBtn.attr(DATA.search) ?: return
        val tx = inputEle.value
        divFlex.clear()
        HttpGet(url).param(searchParam, tx).requestJsonResult { jr ->
            if (jr.OK) {
                val items = jr.dataArray
                for (item in items) {
                    val k = item["key"].toString()
                    val lb = item["label"] as String
                    divFlex.button("btn btn-sm btn-outline-dark m-2") {
                        attr("data-key-value", k)
                        text(lb)
                        val btn = this
                        on("click") {
                            val kVal = btn.attr("data-key-value") ?: ""
                            val lbVal = btn.textContent ?: ""

                            refBtn.closest("div")?.apply {
                                query(".yet-ref-key")?.attr("value", kVal)
                                query(".yet-ref-label")?.attr("value", lbVal)
                            }
                            dlg.hide()
                        }
                    }
                }
            } else {
                window.alert(jr.msg)
            }
        }

    }

}



