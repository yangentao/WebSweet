package dev.entao.web.base

import java.lang.ref.WeakReference
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

typealias Prop = KProperty<*>
typealias Prop0 = KProperty0<*>
typealias Prop1 = KProperty1<*, *>



class FunProps(val block: (KFunction<*>) -> String) {

    operator fun getValue(thisRef: KFunction<*>, property: KProperty<*>): String {
        val key = thisRef.ownerClass!!.qualifiedName!! + "." + thisRef.name + "." + property.name
        return map.getOrPut(key) { block(thisRef) }
    }

    companion object {
        val map = java.util.HashMap<String, String>()
    }
}

class ClassProps(val block: (KClass<*>) -> String) {

    operator fun getValue(thisRef: KClass<*>, property: KProperty<*>): String {
        val key = thisRef.qualifiedName!! + "." + property.name
        return map.getOrPut(key) { block(thisRef) }
    }

    companion object {
        val map = java.util.HashMap<String, String>()
    }
}

class PropProps(val block: (KProperty<*>) -> String) {

    operator fun getValue(thisRef: KProperty<*>, property: KProperty<*>): String {
        val key = thisRef.ownerClass!!.qualifiedName + "." + thisRef.name + "." + property.name
        return map.getOrPut(key) { block(thisRef) }
    }

    companion object {
        val map = java.util.HashMap<String, String>()
    }
}

//根据类型来保存数据, 相当于给KClass添加一个属性, 相当于静态变量
//用于缓存KClass的自定义属性
//KClass<Person>.primaryKey by KClassValue{...}
class KClassValue<VALUE>(val block: (KClass<*>) -> VALUE) {
    private var map = HashMap<KClass<*>, VALUE>()

    @Synchronized
    operator fun getValue(thisRef: KClass<*>, property: KProperty<*>): VALUE {
        if (map.containsKey(thisRef)) {
            @Suppress("UNCHECKED_CAST")
            return map[thisRef] as VALUE
        }
        val v = block(thisRef)
        map[thisRef] = v
        return v
    }

    @Synchronized
    operator fun setValue(thisRef: KClass<*>, property: KProperty<*>, value: VALUE) {
        map[thisRef] = value
    }
}





class WeakRef<T>(value: T?) {
    private var w: WeakReference<T>? = null
    var value: T?
        get() = w?.get()
        set(value) {
            w = if (value == null) null else WeakReference(value)
        }

    init {
        this.value = value
    }


    val isNull: Boolean get() = w?.get() == null

    override fun equals(other: Any?): Boolean {
        if (other is WeakRef<*>) {
            return w?.get() === other.w?.get()
        }
        return false
    }

    override fun hashCode(): Int {
        return w?.get()?.hashCode() ?: 0
    }
}


