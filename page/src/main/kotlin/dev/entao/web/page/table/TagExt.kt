package dev.entao.web.page.table

import dev.entao.web.tag.tag.Tag
import java.sql.ResultSet

fun <T : Any> Tag.tableX(items: List<T>, block: TableXTag<T>.() -> Unit) {
    append(TableXTag<T>(context, TableItemList(items))) {
        this.block()
        this.build()
    }
}

fun Tag.tableX(rs: ResultSet, block: TableXTag<ResultSet>.() -> Unit) {
    append(TableXTag<ResultSet>(context, TableItemResultSet(rs))) {
        this.block()
        this.build()
    }
}