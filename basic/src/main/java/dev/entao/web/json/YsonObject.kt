@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package dev.entao.web.json

import dev.entao.web.base.Prop0
import dev.entao.web.base.decodeAndCast
import dev.entao.web.base.getPropValue
import dev.entao.web.base.lowerCased
import dev.entao.web.base.userName
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class YsonObject(val data: LinkedHashMap<String, YsonValue> = LinkedHashMap(32)) : YsonValue(), Map<String, YsonValue> by data {
    var caseLess = false

    constructor(capcity: Int) : this(LinkedHashMap<String, YsonValue>(capcity))

    constructor(json: String) : this() {
        val p = YsonParser(json)
        val v = p.parse(true)
        if (v is YsonObject) {
            data.putAll(v.data)
        }
    }

    override fun yson(buf: StringBuilder) {
        buf.append("{")
        var first = true
        for ((k, v) in data) {
            if (!first) {
                buf.append(",")
            }
            first = false
            buf.append("\"").append(escapeJson(k)).append("\":")
            v.yson(buf)
        }
        buf.append("}")
    }

    override fun preferBufferSize(): Int {
        return 256
    }

    override fun toString(): String {
        return yson()
    }

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

    operator fun <V> setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        this.data[property.userName] = Yson.toYson(value)
        if (this.gather) {
            if (property is KMutableProperty) {
                if (property !in this._changedProperties) {
                    this._changedProperties.add(property)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline operator fun <reified V> getValue(thisRef: Any?, property: KProperty<*>): V {
        val retType = property.returnType
        val v = if (caseLess) {
            this[property.userName] ?: this[property.userName.lowerCased]
        } else {
            this[property.userName]
        } ?: YsonNull.inst

        if (v !is YsonNull) {
            val pv = YsonDecoder.decodeByType(v, retType, null)
            if (pv != null || retType.isMarkedNullable) {
                return pv as V
            }
        }
        return property.decodeAndCast(null)
    }

    fun removeProperty(p: KProperty<*>) {
        this.data.remove(p.userName)
    }

    fun putString(key: String, value: String?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonString(value)
        }
    }

    fun getString(key: String): String? {
        return when (val v = get(key)) {
            null -> null
            is YsonString -> v.data
            is YsonBool -> v.data.toString()
            is YsonNum -> v.data.toString()
            is YsonNull -> null
            is YsonObject -> v.toString()
            is YsonArray -> v.toString()
            else -> v.toString()
        }
    }

    fun putInt(key: String, value: Int?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getInt(key: String): Int? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toInt()
            is YsonString -> v.data.toIntOrNull()
            else -> null
        }
    }

    fun putLong(key: String, value: Long?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getLong(key: String): Long? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toLong()
            is YsonString -> v.data.toLongOrNull()
            else -> null
        }
    }

    fun putReal(key: String, value: Double?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonNum(value)
        }
    }

    fun getReal(key: String): Double? {
        return when (val v = get(key)) {
            is YsonNum -> v.data.toDouble()
            is YsonString -> v.data.toDoubleOrNull()
            else -> null
        }
    }

    fun putBool(key: String, value: Boolean?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = YsonBool(value)
        }
    }

    fun getBool(key: String): Boolean? {
        val v = get(key) ?: return null
        return BoolYsonConverter.fromYsonValue(v)
    }

    fun putObject(key: String, value: YsonObject?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = value
        }
    }

    fun putObject(key: String, block: YsonObject.() -> Unit) {
        val yo = YsonObject()
        yo.block()
        data[key] = yo
    }

    fun getObject(key: String): YsonObject? {
        return get(key) as? YsonObject
    }

    fun putArray(key: String, value: YsonArray?) {
        if (value == null) {
            data[key] = YsonNull.inst
        } else {
            data[key] = value
        }
    }

    fun getArray(key: String): YsonArray? {
        return get(key) as? YsonArray
    }


    fun putAny(key: String, value: Any?) {
        data[key] = from(value)
    }

    fun getAny(key: String): Any? {
        return get(key)
    }

    fun putNull(key: String) {
        data[key] = YsonNull.inst
    }

    infix fun <V> String.TO(value: V) {
        putAny(this, value)
    }


    infix fun String.TO(value: YsonObject) {
        putObject(this, value)
    }

    infix fun String.TO(value: YsonArray) {
        putArray(this, value)
    }

    operator fun Prop0.unaryPlus() {
        putAny(this.userName, this.getPropValue())
    }


}


@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> KClass<T>.createYsonModel(argValue: YsonObject): T {
    val c = this.constructors.first { it.parameters.size == 1 && it.parameters.first().type.classifier == YsonObject::class }
    return c.call(argValue)
}

fun ysonObject(block: YsonObject.() -> Unit): YsonObject {
    val b = YsonObject()
    b.block()
    return b
}
