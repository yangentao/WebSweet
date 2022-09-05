@file:Suppress("unused")

package dev.entao.web.sql

import dev.entao.web.base.*
import dev.entao.web.json.Yson
import dev.entao.web.json.YsonObject
import java.sql.Connection
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Created by entaoyang@163.com on 2017/3/31.
 */

abstract class OrmModel {
    @Exclude
    val model: OrmMap = OrmMap()
    private val conn: Connection get() = this::class.namedConnection


    operator fun get(key: String): Any? {
        return model[key]
    }

    operator fun get(p: Prop): Any? {
        return model[p.nameSQL]
    }

    fun hasProp(p: KProperty<*>): Boolean {
        return hasProp(p.nameSQL)
    }

    fun hasProp(key: String): Boolean {
        return model.containsKey(key) || model.containsKey(key.lowerCased)
    }

    fun removeProperty(p: KProperty<*>) {
        model.removeProperty(p)
    }

    fun existByKey(): Boolean {
        val w = this.whereByPrimaryKey ?: throw IllegalArgumentException("必须设置主键")
        val cls = this::class
        return conn.querySQL {
            select("1")
            from(cls)
            where(w)
            limit(1)
        }.firstExists
    }

    fun deleteByKey(): Boolean {
        val w = this.whereByPrimaryKey ?: return false
        return conn.delete(this::class, w) > 0
    }

    fun saveByKey(): Boolean {
        return conn.insertOrUpdate(this)
    }

    fun insert(): Boolean {
        val kvs = _PropertiesExists.map { it to it.getPropValue(this) }
        val autoInc = this::class._PrimaryKeys.find { it.hasAnnotation<AutoInc>() } != null
        if (!autoInc) {
            return conn.insert(this::class, kvs)
        }
        val r = conn.insertGenKey(this::class, kvs)
        if (r <= 0L) {
            return false
        }
        val pkProp = this::class._PrimaryKeys.first { it.hasAnnotation<AutoInc>() }
        if (pkProp.returnClass == Long::class) {
            pkProp.setPropValue(this, r)
        } else {
            pkProp.setPropValue(this, r.toInt())
        }
        return true
    }

    fun insertOrUpdate(): Boolean {
        return conn.insertOrUpdate(this)
    }

    fun updateByKey(ps: List<KMutableProperty<*>>): Boolean {
        return if (ps.isNotEmpty()) {
            conn.updateByKey(this, ps)
        } else {
            conn.updateByKey(this)
        } > 0
    }

    fun updateByKey(vararg ps: KMutableProperty<*>): Boolean {
        return if (ps.isNotEmpty()) {
            conn.updateByKey(this, ps.toList())
        } else {
            conn.updateByKey(this)
        } > 0
    }


    override fun toString(): String {
        return Yson.toYson(model).toString()
    }

//	//仅包含有值的列, modMap中出现
//	@Exclude
//	val modelPropertiesExists: List<KMutableProperty<*>>
//		get() {
//			return this::class.modelProperties.filter { model.hasProp(it) }
//		}


}

val <T : OrmModel> T.whereByPrimaryKey: Where?
    get() {
        var w: Where? = null
        this::class._PrimaryKeys.forEach {
            if (hasProp(it)) {
                w = w AND (it EQ it.getPropValue(this))
            } else error("NO PrimaryKey Value ")
        }
        return w
    }

//仅包含有值的列, modMap中出现
val <T : OrmModel> T._PropertiesExists: List<KMutableProperty<*>>
    get() {
        return this::class._SQLProperties.filter { model.hasProp(it) }
    }


fun <T : OrmModel> T.update(block: (T) -> Unit): Boolean {
    val ls = this.model.gather {
        block(this)
    }
    if (ls.isNotEmpty()) {
        return this.updateByKey(ls)
    }
    return false
}


class ModelJson<T : OrmModel>(val model: T) {
    private val propList: ArrayList<Prop> = ArrayList<Prop>()
    private val map = LinkedHashMap<String, Any>()

    init {
        for (p in model._PropertiesExists) {
            propList.add(p)
        }
    }

    fun keyValue(key: String, value: Any) {
        map[key] = value
    }

    fun client() {
        propList.removeIf { it.isHideClient }
    }

    fun include(vararg ps: Prop) {
        propList.addAll(ps)
    }


    fun exclude(vararg ps: Prop) {
        val nameSet = ps.map { it.userName }
        propList.removeIf { it.userName in nameSet }
    }

    fun only(vararg ps: Prop) {
        propList.clear()
        propList.addAll(ps)
    }

    fun filter(block: (Prop) -> Boolean) {
        propList.filter(block)
    }

    fun fill(yo: YsonObject): YsonObject {
        for (p in propList) {
            yo.putAny(p.userName, pval(p))
        }
        return yo
    }

    fun build(): YsonObject {
        val jo = YsonObject()
        for (p in propList) {
            jo.putAny(p.userName, pval(p))
        }
        for ((k, v) in map) {
            jo.putAny(k, v)
        }
        return jo
    }

    private fun pval(p: Prop): Any? {
        val v = p.getPropValue(model)
        val dateAn = p.findAnnotation<DatePattern>()
        if (dateAn != null && v != null) {
            return dateAn.display(v)
        }
        return v
    }
}

fun <T : OrmModel> T.json(block: ModelJson<T>.() -> Unit): YsonObject {
    val m = ModelJson(this)
    m.block()
    return m.build()
}


fun <T : OrmModel> T.jsonClient(): YsonObject {
    return this.json { client() }
}

fun <T : OrmModel> T.jsonClientTo(jo: YsonObject): YsonObject {
    return this.json {
        client()
        fill(jo)
    }
}





