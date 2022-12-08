@file:Suppress("unused", "OPT_IN_USAGE", "FunctionName", "MemberVisibilityCanBePrivate")

import kotlin.js.Json
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


fun Json.str(name: String): String {
    return this[name].toString()
}

fun Json.int(name: String): Int {
    return this[name] as Int
}

fun Json.any(name: String): Any {
    return this[name]!!
}

@Suppress("UNCHECKED_CAST", "PropertyName")
@JsExport
@JsName("JsonResult")
class JsonResult(val json: Json) {
    val code: Int get() = json.getInt("code")
    val msg: String get() = json.getString("msg")

    val OK: Boolean get() = code == 0

    val data: Any? get() = json["data"]
    val dataObject: Json get() = json.getObject("data")
    val dataArray: Array<Json> get() = this.data as Array<Json>
    val dataArrayInt: Array<Int> get() = this.data as Array<Int>
    val dataArrayString: Array<String> get() = this.data as Array<String>
}

@Suppress("UNCHECKED_CAST")
fun Json.getArrayJson(name: String): Array<Json> {
    return this[name] as Array<Json>
}

@Suppress("UNCHECKED_CAST")
fun Json.getArrayJson_(name: String): Array<Json>? {
    return this[name] as? Array<Json>
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun Json.getObject(name: String): Json {
    return this[name] as Json
}

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun Json.getObject_(name: String): Json? {
    return this[name] as? Json
}

fun Json.getString(name: String): String {
    return this[name].toString()
}

fun Json.getString_(name: String): String? {
    return this[name]?.toString()
}

fun Json.getInt(name: String): Int {
    return this[name] as Int
}

fun Json.getInt_(name: String): Int? {
    return this[name] as? Int
}

fun Json.getAny(name: String): Any {
    return this[name]!!
}

fun Json.getAny_(name: String): Any? {
    return this[name]
}

inline operator fun <reified T : Any> Json.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    if (jsTypeOf(value) == jsTypeOf(this)) {
        this[property.name] = value
        return
    }
    when (T::class) {
        String::class, Boolean::class, Long::class, Int::class, Short::class, Byte::class, Double::class, Float::class -> {
            this[property.name] = value
        }
        Array::class -> this[property.name] = value
        else -> error("Json.setValue(), Type Not Supported:${T::class}")
    }

}

inline operator fun <reified T : Any> Json.getValue(thisRef: Any?, property: KProperty<*>): T {
    val v = this[property.name]
    if (v != null) return v as T
    return missValueOf(T::class)
}

inline fun <reified T : Any> missValueOf(cls: KClass<T>): T {
    return when (cls) {
        String::class -> "" as T
        Boolean::class -> false as T
        Long::class, Int::class, Short::class, Byte::class, Double::class, Float::class -> 0 as T
        Array::class -> js("[]") as T
        else -> error("Json.setValue(), Type Not Supported:${T::class}")
    }
}
