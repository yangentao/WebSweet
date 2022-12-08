@file:Suppress("unused")

package dev.entao.web.page.table

import dev.entao.web.base.Prop
import dev.entao.web.base.getPropValue
import dev.entao.web.base.userLabel
import dev.entao.web.base.userName
import dev.entao.web.core.HttpAction
import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.page.ext.fromAction
import dev.entao.web.page.ext.sortParams
import dev.entao.web.sql.OrmModel
import dev.entao.web.sql.Where
import dev.entao.web.sql.eachRow
import dev.entao.web.tag.tag.*
import java.sql.ResultSet
import kotlin.reflect.KClass

const val ITEM_ID = "item-id"

abstract class TableColumnInfo<T>(override val context: HttpContext) : OnHttpContext {
    var sortColumn: String = ""

    abstract fun onHeaderColumn(parentTag: Tag)
    abstract fun onItemColumn(trTag: Tag, tdTag: Tag, item: T)
    abstract fun sortable()
}

abstract class BasicColumnInfo<T : Any>(context: HttpContext, var title: String, var sortCol: String) : TableColumnInfo<T>(context) {
    var maxTextLength: Int = 50

    override fun onHeaderColumn(parentTag: Tag) {
        parentTag.text(title)
    }

    override fun sortable() {
        this.sortColumn = sortCol
    }

    protected fun Tag.trimText(text: String?) {
        var s = text ?: return
        if (s.length > maxTextLength) {
            s = s.substring(0, maxTextLength)
            s += "..."
        }
        this.text(s)
    }
}

class CheckColumnInfo<T>(context: HttpContext) : TableColumnInfo<T>(context) {
    override fun onHeaderColumn(parentTag: Tag) {
        parentTag.apply {
            "style" attr "width:2rem"
            checkbox("yet-checkall") {
                script {
                    """
                        pagescript.checkAllRowsById('${this.idx}');
                    """.trimIndent()
                }
            }
        }
    }

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        tdTag.checkbox {
            "value" attr (trTag.data(ITEM_ID))
        }
    }

    override fun sortable() {
    }

}

typealias TableOperatorColumnCallback = (Tag, HttpAction, String) -> Unit

class OperatorColumnInfo<T>(context: HttpContext) : TableColumnInfo<T>(context) {
    var label: String = "操作"
    private val actionList: ArrayList<HttpAction> = ArrayList()
    private var callback: TableOperatorColumnCallback? = null

    var filterCallback: (HttpAction, T) -> Boolean = { _, _ -> true }

    override fun sortable() {
    }

    override fun onHeaderColumn(parentTag: Tag) {
        parentTag.text(label)
    }

    override fun onItemColumn(trTag: Tag, tdTag: Tag, item: T) {
        tdTag.apply {
            div("d-inline", "fw-blod") {
                for (ac in actionList) {
                    if (!filterCallback(ac, item)) continue
                    a("yet-action") {
                        val itemKey = trTag.data(ITEM_ID)
                        fromAction(ac, itemKey)
                        callback?.invoke(this, ac, itemKey)
                    }
                }
            }
        }
    }

    fun filter(block: (HttpAction, T) -> Boolean) {
        filterCallback = block
    }

    fun list(vararg acList: HttpAction): OperatorColumnInfo<T> {
        actionList.addAll(acList)
        return this
    }

    fun callback(block: TableOperatorColumnCallback) {
        this.callback = block
    }

}
typealias TableItemIDCallback<T> = (T) -> String

interface TableItems {
    fun each(block: (Any) -> Unit)
}

class TableItemList(val items: List<Any>) : TableItems {
    override fun each(block: (Any) -> Unit) {
        for (item in items) {
            block(item)
        }
    }
}

class TableItemResultSet(val items: ResultSet) : TableItems {
    override fun each(block: (Any) -> Unit) {
        items.eachRow(block)
    }
}

@Suppress("UNCHECKED_CAST")
class TableXTag<T : Any>(context: HttpContext, private val itemsData: TableItems) : TableTag(context) {
    private val columnList: ArrayList<TableColumnInfo<T>> = ArrayList()
    private var itemIDCallback: TableItemIDCallback<T>? = null
    private var sortAction: HttpAction? = null

    init {
        classAdd("table", "table-hover")
    }

    fun itemIDBlock(block: TableItemIDCallback<T>) {
        itemIDCallback = block
    }

    fun itemIDProp(prop: Prop) {
        itemIDCallback = {
            prop.getPropValue(it).toString()
        }
    }

    fun sortAction(action: HttpAction) {
        this.sortAction = action
        val sp = action.sortParams ?: return

        this.data("sort-action", context.uriAction(action))
        this.data("sort-param-col", sp.col)
        this.data("sort-param-dir", sp.dir)
        this.data("sort-col-value", context[sp.col] ?: "")
        this.data("sort-dir-value", context[sp.dir] ?: "")
    }

    fun columns(cs: List<TableColumnInfo<T>>) {
        this.columnList.addAll(cs)
    }

    fun column(col: TableColumnInfo<T>) {
        this.columnList.add(col)
    }

    fun columnCheck() {
        this.columnList += CheckColumnInfo(context)
    }

    fun columnProp(prop: Prop): PropColumnInfo<T> {
        val a = PropColumnInfo<T>(context, prop)
        this.columnList += a
        return a
    }

    fun columnDate(prop: Prop, format: String? = null): DateColumnInfo<T> {
        val a = DateColumnInfo<T>(context, prop, format)
        this.columnList += a
        return a
    }

    fun columnDownload(prop: Prop, title: String = ""): DownloadColumnInfo<T> {
        val a = DownloadColumnInfo<T>(context, prop, title)
        this.columnList += a
        return a
    }

    fun columnKey(title: String, key: String): KeyColumnInfo<T> {
        val a = KeyColumnInfo<T>(context, title, key)
        this.columnList += a
        return a
    }

    fun columnImage(prop: Prop, title: String = prop.userLabel, sortCol: String = prop.userName): ImageColumnInfo<T> {
        val a = ImageColumnInfo<T>(context, prop, title, sortCol)
        this.columnList += a
        return a
    }

    fun columnRef(prop: Prop, refTable: KClass<out OrmModel>, refProp: Prop, refDisplay: Prop? = null, filter: Where? = null): RefColumnInfo<T> {
        val a = RefColumnInfo<T>(context, prop, refTable, refProp, refDisplay, filter)
        this.columnList += a
        return a
    }

    fun columnText(prop: Prop): TextColumnInfo<T> {
        val a = TextColumnInfo<T>(context, prop)
        this.columnList += a
        return a
    }

    fun columnForeignKey(prop: Prop, title: String = prop.userLabel): ForeignKeyColumnInfo<T> {
        val a = ForeignKeyColumnInfo<T>(context, prop)
        a.title = title
        this.columnList += a
        return a
    }

    fun columnCustom(title: String, sortCol: String = "", block: (T) -> String): CustomColumnInfo<T> {
        val a = CustomColumnInfo<T>(context, title, sortCol, block)
        this.columnList += a
        return a
    }

    fun columnAction(vararg acList: HttpAction): OperatorColumnInfo<T> {
        val a = OperatorColumnInfo<T>(context).list(*acList)
        this.columnList += a
        return a
    }

    private fun buildHref(link: Tag, ac: HttpAction, colName: String): String {
        val sortCol = this.data("sort-param-col")
        val sortDir = this.data("sort-param-dir")
        val sortColValue = this.data("sort-col-value")
        val sortDirValue = this.data("sort-dir-value")
        val newDirVal = if (sortColValue == colName) {
            if (sortDirValue == "0") {
                link.unsafe("&Delta;")
                "1"
            } else {
                link.unsafe("&nabla;")
                "0"
            }
        } else "0"
        return context.uriActionParams(ac, setOf(sortCol, sortDir), listOf(sortCol to colName, sortDir to newDirVal))
    }

    fun build() {
        val sortAc = this.sortAction
        tr {
            for (ci in columnList) {
                th {
                    scope = "col"
                    if (sortAc == null || ci.sortColumn.isEmpty()) {
                        ci.onHeaderColumn(this)
                    } else {
                        this.data("sort-column", ci.sortColumn)
                        a("yet-action") {
                            ci.onHeaderColumn(this)
                            href = buildHref(this, sortAc, ci.sortColumn)
                        }
                    }

                }
            }
        }
        itemsData.each { item ->
            item as T
            tr {
                data(ITEM_ID, itemIDCallback?.invoke(item) ?: "")
                val trTag = this
                for (ci in columnList) {
                    td {
                        ci.onItemColumn(trTag, this, item)
                    }
                }
            }
        }
    }

}