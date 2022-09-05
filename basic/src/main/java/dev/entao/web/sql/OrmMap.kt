package dev.entao.web.sql

import dev.entao.web.base.Prop
import dev.entao.web.base.decodeAndCast
import dev.entao.web.base.userName
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2017/4/20.
 */

class OrmMap(capacity: Int = 32) : HashMap<String, Any?>(capacity) {

    private val _changedProperties = ArrayList<KMutableProperty<*>>(8)
    private var gather: Boolean = false

    @Synchronized
    fun gather(block: () -> Unit): ArrayList<KMutableProperty<*>> {
        this.gather = true
        this._changedProperties.clear()
        block()
        val ls = ArrayList<KMutableProperty<*>>(_changedProperties)
        this.gather = false
        return ls
    }

    fun hasProp(p: Prop): Boolean {
        return this.containsKey(p.nameSQL) || this.containsKey(p.userName)
    }

    fun removeProperty(p: KProperty<*>) {
        this.remove(p.nameSQL)
        this.remove(p.userName)
    }

    operator fun get(prop: KProperty<*>): Any? {
        return this.getValue(this, prop)
    }

    operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this[property.nameSQL] = value
        if (this.gather) {
            if (property is KMutableProperty) {
                if (property !in this._changedProperties) {
                    this._changedProperties.add(property)
                }
            }
        }
    }

    //select user.id, user.name from user
    //sqlite 返回的结果集中, columnLabel 是 user.id, user.name
    //mysql 返回的结果集 columnLabel 是 id, name
    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        val v = this[property.nameSQL] ?: this[property.userName] ?: this[property.fullNameSQL]
        return property.decodeAndCast(v)
    }

}