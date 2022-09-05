package dev.entao.web.sql

import dev.entao.web.base.*
import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonObject
import dev.entao.web.json.YsonValue
import dev.entao.web.log.logd
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties

//class Account {
//    var id: Int = 0
//    var name: String? = null
//    var phone: String? = null
//
//    override fun toString(): String {
//        return "Account{id=$id, name=$name, phone=$phone}"
//    }
//}
//
//fun main() {
//    ConnPick.addSourceMySQL(
//            "hare",
//            "hare",
//            "hare",
//            "jdbc:mysql://192.168.3.100:3306/hare?useSSL=false&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Hongkong"
//    )
//    val rs = ConnPick.connection.querySQL {
//        from("Account")
//        select("id", "name", "phone")
//    }
//    logd("Accounts: ")
////    logd(rs.firstJson?.toString())
//    logd(rs.firstModel<Account>()?.toString())
//}


fun interface CursorToModel {
    fun fromCursor(rs: ResultSet)
}


fun <R : Any> ResultSet.firstRow(block: (ResultSet) -> R): R? {
    return this.useResultSet {
        if (it.next()) {
            block(it)
        } else null
    }
}

fun <T : Any> ResultSet.toList(block: (ResultSet) -> T): ArrayList<T> {
    val ls = ArrayList<T>(this.fetchSize + 8)
    this.useResultSet {
        while (it.next()) {
            ls += block(it)
        }
    }
    return ls
}

fun ResultSet.eachRow(block: (ResultSet) -> Unit) {
    this.useResultSet { rs ->
        while (rs.next()) {
            block(rs)
        }
    }
}


val ResultSet.toYsonArray: YsonArray
    get() {
        val arr = YsonArray(256)
        val meta = this.metaData
        this.eachRow {
            arr.data += it.currentYsonObject(meta)
        }
        return arr
    }
val ResultSet.toListYsonObject: ArrayList<YsonObject>
    get() {
        val meta = this.metaData
        return toList {
            it.currentYsonObject(meta)
        }
    }

//--------------------Orm-------------------
val ResultSet.firstOrmMap: OrmMap? get() = this.firstRow { it.currentOrmMap(it.metaData) }

inline fun <reified T : OrmModel> ResultSet.firstOrmModel(): T? {
    val map = this.firstOrmMap ?: return null
    val m: T = T::class.createInstanceX(map)
    m.model.putAll(map)
    return m
}

fun ResultSet.toListOrmMap(): ArrayList<OrmMap> {
    val meta = this.metaData
    return this.toList { it.currentOrmMap(meta) }
}

fun <T : OrmModel> ResultSet.toListOrmModel(block: () -> T): List<T> {
    return this.toListOrmMap().map {
        block().apply { model.putAll(it) }
    }
}

inline fun <reified T : OrmModel> ResultSet.toListOrmModel(): List<T> {
    return this.toListOrmMap().map {
        val m: T = T::class.createInstanceX()
        m.model.putAll(it)
        m
    }
}

fun ResultSet.currentOrmMap(meta: ResultSetMetaData): OrmMap {
    val map = OrmMap()
    for (i in 1..meta.columnCount) {
        map[meta.getColumnLabel(i)] = this.getObject(i)
    }
    return map
}

//---------------------------------------------------------------------
inline fun <reified T : Any> ResultSet.firstModel(): T? {
    val m = this.metaData
    return this.firstRow { it.currentModel(m) }
}

inline fun <reified T : Any> ResultSet.toListModel(): List<T> {
    val m = this.metaData
    return this.toList { it.currentModel(m) }
}


//=============first=================

val ResultSet.firstExists: Boolean
    get() {
        return this.useResultSet {
            this.next()
        }
    }


val ResultSet.firstJson: YsonObject? get() = this.firstRow { it.currentYsonObject(it.metaData) }


fun ResultSet.firstString(col: Int = 1): String? {
    return firstRow { it.getString(col) }
}

fun ResultSet.firstString(col: String): String? {
    return firstRow { it.getString(col) }
}

fun ResultSet.firstInt(col: Int = 1): Int? {
    return firstRow { it.getInt(col) }
}

fun ResultSet.firstInt(col: String): Int? {
    return firstRow { it.getInt(col) }
}

fun ResultSet.firstLong(col: Int = 1): Long? {
    return firstRow { it.getLong(col) }
}

fun ResultSet.firstLong(col: String): Long? {
    return firstRow { it.getLong(col) }
}

fun ResultSet.firstDouble(col: Int = 1): Double? {
    return firstRow { it.getDouble(col) }
}

fun ResultSet.firstDouble(col: String): Double? {
    return firstRow { it.getDouble(col) }
}


fun <R> ResultSet.useResultSet(block: (ResultSet) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this.closeWithStatement()
        } catch (_: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            this.closeWithStatement()
        }
    }
}


inline fun <reified T : Any> ResultSet.currentModel(meta: ResultSetMetaData): T {
    val m: T = T::class.createInstance()
    if (m is CursorToModel) {
        m.fromCursor(this)
        return m
    }

    val propList: List<KMutableProperty<*>> = T::class.memberProperties.filter { it.isPublic && it is KMutableProperty<*> }.map { it as KMutableProperty<*> }
    for (i in 1..meta.columnCount) {
        val label = meta.getColumnLabel(i)
        val prop = propList.firstOrNull { it.userName == label } ?: continue
        val pvalue: Any? = when (prop.returnClass) {
            String::class -> this.getString(i)
            Long::class -> this.getLong(i)
            Int::class -> this.getInt(i)
            Short::class -> this.getShort(i)
            Byte::class -> this.getByte(i)
            Boolean::class -> this.getBoolean(i)
            Double::class -> this.getDouble(i)
            Float::class -> this.getFloat(i)
            ByteArray::class -> this.getBlob(i)
            YsonObject::class -> {
                val s = this.getString(i)
                if (s != null) YsonObject(s) else null
            }

            YsonArray::class -> {
                val s = this.getString(i)
                if (s != null) YsonArray(s) else null
            }

            else -> error("NOT support property: $prop")

        }
        if (!prop.returnType.isMarkedNullable && pvalue == null) {
            continue
        }
        prop.setPropValue(m, pvalue)
    }
    return m
}

fun ResultSet.currentYsonObject(meta: ResultSetMetaData): YsonObject {
    val yo = YsonObject(meta.columnCount + 2)
    for (i in 1..meta.columnCount) {
        val label = meta.getColumnLabel(i)
        val typeName = meta.getColumnTypeName(i)
        val value: Any? = if (typeName in jsonTypes) {
            val js = this.getString(i)?.trim()
            if (js == null || js.isEmpty()) {
                null
            } else if (js.startsWith("{")) {
                YsonObject(js)
            } else if (js.startsWith("[")) {
                YsonArray(js)
            } else {
                null
            }
        } else {
            this.getObject(i)
        }
        yo.putAny(label, value)
    }
    return yo
}

/**
 * A ResultSet object is automatically closed by the Statement object that generated it
 * when that Statement object is closed, re-executed, or is used to retrieve the next result from a sequence of multiple results.
 */
fun ResultSet.closeWithStatement() {
    this.statement?.closeSafe()
}

private val jsonTypes: Set<String> = setOf("json", "JSON", "jsonb", "JSONB")


fun ResultSet.dump() {
    val meta = this.metaData
    val sb = StringBuilder(512)
    this.eachRow {
        sb.setLength(0)
        for (i in 1..meta.columnCount) {
            val label = meta.getColumnLabel(i)
            val value = this.getObject(i)
            sb.append(label).append("=").append(value).append(", ")
        }
        logd(sb.toString())
    }
}


class ResultSetIterator(private val rs: ResultSet) : Iterator<ResultSet> {
    override operator fun next(): ResultSet {
        return rs
    }

    override operator fun hasNext(): Boolean {
        val b = rs.next()
        if (!b) {
            rs.closeWithStatement()
        }
        return b
    }
}

operator fun ResultSet.iterator(): Iterator<ResultSet> {
    return ResultSetIterator(this)
}


inline operator fun <reified T> ResultSet.get(key: String): T {
    return when (T::class) {
        String::class -> this.getString(key)
        Long::class -> this.getLong(key)
        Int::class -> this.getInt(key)
        Short::class -> this.getShort(key)
        Byte::class -> this.getByte(key)
        Float::class -> this.getFloat(key)
        Double::class -> this.getDouble(key)
        Boolean::class -> this.getBoolean(key)
        ByteArray::class -> this.getBlob(key)
        java.sql.Date::class -> this.getDate(key)
        java.sql.Time::class -> this.getTime(key)
        java.sql.Timestamp::class -> this.getTimestamp(key)
        YsonObject::class -> YsonObject(getString(key) ?: "")
        YsonArray::class -> YsonArray(getString(key) ?: "")
        Any::class -> this.getObject(key)
        else -> error("Unsupport type ${T::class}")
    } as T
}

inline operator fun <reified T> ResultSet.get(idx: Int): T {
    return when (T::class) {
        String::class -> this.getString(idx)
        Long::class -> this.getLong(idx)
        Int::class -> this.getInt(idx)
        Short::class -> this.getShort(idx)
        Byte::class -> this.getByte(idx)
        Float::class -> this.getFloat(idx)
        Double::class -> this.getDouble(idx)
        Boolean::class -> this.getBoolean(idx)
        ByteArray::class -> this.getBlob(idx)
        java.sql.Date::class -> this.getDate(idx)
        java.sql.Time::class -> this.getTime(idx)
        java.sql.Timestamp::class -> this.getTimestamp(idx)
        YsonObject::class -> YsonObject(getString(idx) ?: "")
        YsonArray::class -> YsonArray(getString(idx) ?: "")
        Any::class -> this.getObject(idx)
        else -> error("Unsupport type ${T::class}")
    } as T
}

fun ResultSet.getJson(col: Int): YsonValue? {
    val js = this.getString(col) ?: return null
    return if (js.startsWith("{")) {
        YsonObject(js)
    } else if (js.startsWith("[")) {
        YsonArray(js)
    } else {
        null
    }
}

fun ResultSet.getJsonObject(col: Int): YsonObject? {
    val js = this.getString(col) ?: return null
    return if (js.startsWith("{")) {
        YsonObject(js)
    } else {
        null
    }
}

fun ResultSet.getJsonArray(col: Int): YsonArray? {
    val js = this.getString(col) ?: return null
    return if (js.startsWith("[")) {
        YsonArray(js)
    } else {
        null
    }
}