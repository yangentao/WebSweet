@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.entao.web.sql.ext

import dev.entao.web.base.decodeAndCast
import dev.entao.web.base.encodeToString
import dev.entao.web.base.userName
import dev.entao.web.sql.ConnPick
import dev.entao.web.sql.closeWithStatement
import dev.entao.web.sql.createTable
import dev.entao.web.sql.firstExists
import dev.entao.web.sql.firstString
import dev.entao.web.sql.query
import dev.entao.web.sql.tableExists
import dev.entao.web.sql.trans
import dev.entao.web.sql.update
import java.sql.Connection
import kotlin.collections.set
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2018/8/26.
 */

class MapTable(val tableName: String, val connName: String? = null) {

    val mapCon: Connection
        get() = ConnPick.pick(connName)


    init {
        mayCreateTable(mapCon, tableName)
    }

    inline operator fun <reified V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        if (value == null) {
            this.remove(property.userName)
        } else {
            val s: String? = property.encodeToString(value)
            if (s == null) {
                remove(property.userName)
            } else {
                this.put(property.userName, s)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        val s: String? = this[property.userName]
        return property.decodeAndCast(s)
    }

    fun putAll(map: Map<String, String>) {
        mapCon.trans { _ ->
            map.forEach {
                put(it.key, it.value)
            }
        }
    }

    fun put(key: String, value: String) {
        if (has(key)) {
            mapCon.update("UPDATE $tableName SET value_ = ? WHERE key_ = ?", listOf(value, key))
        } else {
            mapCon.update("INSERT INTO $tableName(key_,value_) VALUES(?,?)", listOf(key, value))
        }
    }

    fun remove(key: String): Int {
        return mapCon.update("DELETE FROM $tableName WHERE key_=?", listOf(key))
    }

    fun has(key: String): Boolean {
        return mapCon.query("SELECT 1 FROM $tableName WHERE key_ = ?", listOf(key)).firstExists
    }

    operator fun get(key: String): String? {
        return mapCon.query("SELECT value_ FROM $tableName WHERE key_ = ? LIMIT 1", listOf(key)).firstString()
    }

    operator fun set(key: String, value: String?) {
        if (value == null) {
            remove(key)
        } else {
            put(key, value)
        }
    }

    val mapValue: HashMap<String, String>
        get() {
            val m = HashMap<String, String>()
            val r = mapCon.query("SELECT key_, value_ FROM $tableName", emptyList())
            while (r.next()) {
                val k = r.getString(1)
                val v = r.getString(2) ?: ""
                m[k] = v
            }
            r.closeWithStatement()
            return m
        }

    companion object {
        private val nameSet = HashSet<String>()

        @Synchronized
        private fun mayCreateTable(mapCon: Connection, tableName: String) {
            if (tableName in nameSet) {
                return
            }
            nameSet.add(tableName)

            if (mapCon.tableExists(tableName)) {
                return
            }
            mapCon.createTable(tableName, listOf("key_ VARCHAR(256) PRIMARY KEY", "value_ VARCHAR(1024)"))
        }
    }

}