import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTableElement


//带有data-keys-name的a标签, 点击时, 携带选中的行的key参数, href="http://xxx.com/a"  => http://xx.com/a?keys=1,2,3
@JsExport
@JsName("appendCheckedValues")
fun appendCheckedValues(eid: String) {
    val ele: HTMLAnchorElement = findElementById(eid) as? HTMLAnchorElement ?: return
    ele.onclick = {
        val table: HTMLTableElement? = ele.nearest()
        val values: String = table?.dataCheckedValues ?: ""
        if (table == null || values.isEmpty()) {
            false
        } else {
            var url = ele.attr("href")!!
            val idx = url.indexOf('?')
            if (idx > 0) {
                url = url.substring(0, idx)
            }
            val keyName = ele.attr(DATA.key)!!
            ele.href = "$url?$keyName=$values"
            val cs = ele.attr(DATA.confirm)
            cs == null || cs.isEmpty() || window.confirm(cs)
        }

    }
}

@JsExport
@JsName("checkAllRowsById")
fun checkAllRowsById(eid: String) {
    val cb: HTMLInputElement = findElementById(eid) as? HTMLInputElement ?: return
    val tb: HTMLTableElement = cb.closest("table") as? HTMLTableElement ?: return
    val ls: List<HTMLInputElement> = tb.all("td input[type='checkbox']")
    if (ls.isEmpty()) return
    cb.onSelf("click") { ele ->
        ls.forEach {
            it.checked = ele.checked
        }
        makeTableCheckedValues(tb)
    }
    ls.forEach {
        it.on("click") {
            makeTableCheckedValues(tb)
        }
    }
}

internal var HTMLTableElement.dataCheckedValues: String
    get() = this.data("checked-values") ?: ""
    set(value) {
        this.data("checked-values", value)
    }

internal fun makeTableCheckedValues(table: HTMLTableElement) {
    val ls = ArrayList<String>()
    table.all<HTMLInputElement>("td input[type='checkbox']").forEach { ele ->
        if (ele.checked) {
            ls += ele.value
        }
    }
    val s = ls.joinToString(",")
    table.dataCheckedValues = s
}
