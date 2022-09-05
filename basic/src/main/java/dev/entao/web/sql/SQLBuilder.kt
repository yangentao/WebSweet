@file:Suppress("FunctionName", "PropertyName", "unused")

package dev.entao.web.sql

import dev.entao.web.base.Prop
import dev.entao.web.base.plusAssign
import java.sql.Connection
import java.sql.ResultSet
import kotlin.reflect.KClass


@DslMarker
annotation class SQLMarker


fun Connection.dump(block: SQLBuilder.() -> Unit) {
    val q = SQL(block)
    this.query(q.sqlText, q.argList).dump()
}

fun Connection.querySQL(block: SQLBuilder.() -> Unit): ResultSet {
    val q = SQLBuilder()
    q.block()
    return this.query(q.sqlText, q.argList)
}

fun SQL(block: SQLBuilder.() -> Unit): SQLBuilder {
    val a = SQLBuilder()
    a.block()
    return a
}

fun UNION(vararg qs: SQLBuilder): SQLArgs {
    val sql = qs.joinToString(" UNION ") { it.sqlText }
    val ls = ArrayList<Any?>()
    qs.forEach {
        ls.addAll(it.argList)
    }
    return SQLArgs(sql, ls)
}

fun UNION_ALL(vararg qs: SQLBuilder): SQLArgs {
    val sql = qs.joinToString(" UNION ALL ") { it.sqlText }
    val ls = ArrayList<Any?>()
    qs.forEach {
        ls.addAll(it.argList)
    }
    return SQLArgs(sql, ls)
}

//不支持子查询
class SQLBuilder {
    val argList = ArrayList<Any>()
    val sqlText: String get() = toString()

    private var distinct: Boolean = false
    private val selectList = ArrayList<String>()
    private var fromBuffer: String = ""
    private var whereBuffer: String = ""
    private var havingBuffer: String = ""
    private var groupByBuffer: String = ""
    private var orderByBuffer: String = ""
    private var limitBuffer: String = ""
    private var windowBuffer: String = ""
    private var joinBuffer: String = ""

    override fun toString(): String {
        val selBuf = StringBuilder(128)
        selBuf += "SELECT "
        if (distinct) {
            selBuf += " DISTINCT "
        }
        selBuf += if (selectList.isEmpty()) {
            "* "
        } else {
            selectList.joinToString(", ")
        }
        if (whereBuffer.isNotEmpty()) {
            whereBuffer = "WHERE $whereBuffer"
        }
        val ls = listOf(selBuf.toString(), fromBuffer, joinBuffer, whereBuffer, groupByBuffer, havingBuffer, windowBuffer, orderByBuffer, limitBuffer)
        return ls.filter { it.isNotEmpty() }.joinToString("\n")
    }

    private val Any.sql: String
        get() {
            return when (this) {
                is String -> this
                is Prop -> this.fullNameSQL
                is KClass<*> -> this.nameSQL
                is AggreFun -> this.toString()
                is SQLBuilder -> {
                    this.argList.addAll(this.argList)
                    this.sqlText
                }
                else -> error("NOT Support select type:$this")
            }
        }

    fun subquery(block: SQLBuilder.() -> Unit): String {
        val b = SQLBuilder()
        b.block()
        this.argList.addAll(b.argList)
        return "(" + b.sqlText + ")"
    }

    val OrmModelClass<*>.ALL: String get() = this.tableClass.nameSQL + ".*"


    val Any.ASC: String
        get() {
            return "${this.sql} ASC"
        }
    val Any.DESC: String
        get() {
            return "${this.sql} DESC"
        }

    fun Any.ORDER(asc: Boolean): String {
        return if (asc) this.ASC else this.DESC
    }

    infix fun Any.AS(other: String): String {
        return "${this.sql} AS $other "
    }

//	infix fun String.AS(other: String): String {
//		return "$this AS $other "
//	}
//
//	infix fun Prop.AS(other: String): String {
//		return "${this.fullNameSQL} AS $other"
//	}
//
//	infix fun KClass<*>.AS(other: String): String {
//		return "${this.nameSQL} AS $other"
//	}
//
//	infix fun WinFun.AS(other: String): String {
//		return this.toString() AS other
//	}

    fun select(vararg cs: Any) {
        selectList += if (cs.isEmpty()) {
            "* "
        } else {
            cs.joinToString(", ") {
                it.sql
            }
        }
    }

    fun selectAll() {
        select("*")
    }


    fun selectDistinct(vararg cs: Any) {
        distinct = true
        select(*cs)
    }

    fun from(vararg tables: Any) {
        fromBuffer = "FROM " + tables.joinToString(", ") {
            it.sql
        }
    }

    fun where(vararg ws: Where?) {
        val w: Where = AND_ALL(null, *ws) ?: return
        whereBuffer = if (whereBuffer.isEmpty()) {
            w.value
        } else {
            "(" + whereBuffer + ") AND (" + w.value + ")"
        }
        argList.addAll(w.args)
    }

    fun orderBy(vararg ods: Any) {
        val od = OrderBy(ods.toList()).toString()
        if (od.isNotEmpty()) {
            orderByBuffer = od
        }
    }

    fun groupBy(vararg cols: Any) {
        val s = cols.joinToString(",") { it.sql }
        if (s.isNotEmpty()) {
            groupByBuffer = "GROUP BY $s"
        }
    }

    fun having(vararg ws: Where?) {
        val w = AND_ALL(null, *ws)
        if (w != null) {
            havingBuffer = "HAVING " + w.value
            argList.addAll(w.args)
        }
    }

    fun limit(size: Int, offset: Int = 0) {
        if (size > 0 && offset >= 0) {
            limitBuffer = "LIMIT $size OFFSET $offset"
        }
    }

    fun window(name: String, block: OverClause.() -> Unit) {
        val od = OverClause()
        od.block()
        windowBuffer = "WINDOW $name AS ($od)"
    }

    fun window(block: WindowBuilder.() -> Unit) {
        val w = WindowBuilder()
        w.block()
        windowBuffer = "WINDOW $w"
    }

    fun join(vararg tables: Any) {
        joinBuffer = if (tables.size == 1) {
            "JOIN " + tables.joinToString(",") { it.sql }
        } else {
            "JOIN (" + tables.joinToString(",") { it.sql } + ")"
        }

    }

    fun leftJoin(vararg tables: Any) {
        joinBuffer = if (tables.size == 1) {
            "LEFT JOIN " + tables.joinToString(",") { it.sql }
        } else {
            "LEFT JOIN (" + tables.joinToString(",") { it.sql } + ")"
        }
    }

    fun rightJoin(vararg tables: Any) {
        joinBuffer = if (tables.size == 1) {
            "RIGHT JOIN " + tables.joinToString(",") { it.sql }
        } else {
            "RIGHT JOIN (" + tables.joinToString(",") { it.sql } + ")"
        }
    }

    fun on(onClause: String) {
        joinBuffer += " ON ($onClause)"
    }

    fun on(block: OnBuild.() -> String) {
        val b = OnBuild()
        val s = b.block()
        if (s.isNotEmpty()) {
            on(s)
        }
    }

    inner class OnBuild {

        infix fun Any.EQ(s: Any): String {
            return "${this.sql} = ${s.sql}"
        }

        infix fun String.AND(s: String): String {
            return "$this AND $s"
        }
    }

    infix fun WinFun.over(windowName: String): String {
        return if (windowName.isEmpty()) {
            "$this OVER()"
        } else {
            "$this OVER $windowName"
        }
    }

    infix fun WinFun.over(block: OverClause.() -> Unit): String {
        val od = OverClause()
        od.block()
        return "$this OVER ($od)"
    }

    fun MAX(col: Any): AggreFun {
        return AggreFun("MAX", listOf(col.sql))
    }

    fun MIN(col: Any): AggreFun {
        return AggreFun("MIN", listOf(col.sql))
    }

    fun AVG(col: Any): AggreFun {
        return AggreFun("AVG", listOf(col.sql))
    }

    fun SUM(col: Any): AggreFun {
        return AggreFun("SUM", listOf(col.sql))
    }

    fun COUNT(col: Any): AggreFun {
        return AggreFun("COUNT", listOf(col.sql))
    }

    fun CUME_DIST(): WinFun {
        return WinFun("CUME_DIST", emptyList())
    }

    fun FIRST_VALUE(col: Any): WinFun {
        return WinFun("FIRST_VALUE", listOf(col.sql))
    }

    fun LAST_VALUE(col: Any): WinFun {
        return WinFun("LAST_VALUE", listOf(col.sql))
    }

    fun NTH_VALUE(col: Any, n: Int): WinFun {
        return WinFun("NTH_VALUE", listOf(col.sql, n.toString()))
    }

    fun ROW_NUMBER(): WinFun {
        return WinFun("ROW_NUMBER", emptyList())
    }

    fun RANK(): WinFun {
        return WinFun("RANK", emptyList())
    }

    fun DENSE_RANK(): WinFun {
        return WinFun("DENSE_RANK", emptyList())
    }

    @SQLMarker
    inner class WindowBuilder {
        private val list = ArrayList<String>()
        infix fun String.AS(block: OverClause.() -> Unit) {
            val od = OverClause()
            od.block()
            list += "$this AS ($od)"
        }

        infix fun String.asWindow(other: String) {
            list += "$this AS $other"
        }

        override fun toString(): String {
            return list.joinToString(", ")
        }
    }

    inner class AggreFun(name: String, args: List<String>) : WinFun(name, args)

    @SQLMarker
    open inner class WinFun(val name: String, val args: List<String>) {

        override fun toString(): String {
            return "$name(${args.joinToString(",")})"
        }
    }


    @SQLMarker
    inner class OverClause {
        private var sb = StringBuilder(128)

        fun partitionBy(vararg cols: Any) {
            if (cols.isNotEmpty()) {
                sb += "PARTITION BY "
                sb += cols.joinToString(", ") { it.sql }
            }
        }

        fun orderBy(vararg ods: Any) {
            if (sb.isNotEmpty()) {
                sb.append(" ")
            }
            sb += OrderBy(ods.toList()).toString()
        }

        override fun toString(): String {
            return sb.toString()
        }
    }

    @SQLMarker
    inner class OrderBy(private val ods: List<Any>) {
        override fun toString(): String {
            if (ods.isEmpty()) return ""
            return "ORDER BY " + ods.joinToString(", ") { it.sql }
        }
    }


}