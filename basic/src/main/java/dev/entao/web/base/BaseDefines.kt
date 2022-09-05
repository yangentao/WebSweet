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


//TODO delete LazyValue
//只保存值
//var name:String by CacheValue{...}
class LazyValue<VALUE>(val block: () -> VALUE) {
    private var v: VALUE? = null
    private var inited = false

    @Synchronized
    operator fun getValue(thisRef: Any?, property: KProperty<*>): VALUE {
        if (!inited) {
            v = block()
            inited = true
        }
        return v!!
    }

    @Synchronized
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: VALUE) {
        inited = true
        v = value
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

//使用属性的类+属性的名称做为键来存储数据, 相当于KProperty的静态变量
//val KProperty<*>.sqlName by KPropValue{ sqlEscape(it.name)}
class KPropValue<VALUE>(val block: (KProperty<*>) -> VALUE) {
    private var map = HashMap<String, VALUE>()
    private fun makeKey(thisRef: KProperty<*>, property: KProperty<*>): String {
        return thisRef.ownerClass!!.qualifiedName!! + "." + thisRef.name + "." + property.name
    }

    @Synchronized
    operator fun getValue(thisRef: KProperty<*>, property: KProperty<*>): VALUE {
        val key = makeKey(thisRef, property)
        if (map.containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            return map[key] as VALUE
        }
        val v = block(thisRef)
        map[key] = v
        return v
    }

    @Synchronized
    operator fun setValue(thisRef: KProperty<*>, property: KProperty<*>, value: VALUE) {
        val key = makeKey(thisRef, property)
        map[key] = value
    }
}

// 相当于静态变量
//Person::add.paramNames:List<String> by KFunValue{...}
class KFunValue<VALUE>(val block: (KFunction<*>) -> VALUE) {
    private var map = HashMap<String, VALUE>()

    private fun makeKey(thisRef: KFunction<*>, property: KProperty<*>): String {
        return thisRef.ownerClass!!.qualifiedName!! + "." + thisRef.name + "." + property.name
    }

    @Synchronized
    operator fun getValue(thisRef: KFunction<*>, property: KProperty<*>): VALUE {
        val key = makeKey(thisRef, property)
        if (map.containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            return map[key] as VALUE
        }
        val v = block(thisRef)
        map[key] = v
        return v
    }

    @Synchronized
    operator fun setValue(thisRef: KFunction<*>, property: KProperty<*>, value: VALUE) {
        val key = makeKey(thisRef, property)
        map[key] = value
    }
}

//根据对象存放值, 相当于对象的属性
//不能存放null
//OK: var KClass<*>.myvalue:String by ObjectValue{ "abc" }
//BAD: var KClass<*>.myvalue:String? by ObjectValue{ "abc" }
class ObjectValue<THIS, VALUE>(val block: (THIS) -> VALUE) {
    private var map = HashMap<WeakRef<THIS>, VALUE>()
    private var readCount: Int = 0

    @Synchronized
    operator fun getValue(thisRef: THIS, property: KProperty<*>): VALUE {
        readCount += 1
        if (readCount > 10000) {
            readCount = 0
            val ite = map.iterator()
            while (ite.hasNext()) {
                val e = ite.next()
                if (e.key.isNull) {
                    ite.remove()
                }
            }
        }
        val w = WeakRef(thisRef)
        if (map.containsKey(w)) {
            @Suppress("UNCHECKED_CAST")
            return map[w] as VALUE
        }
        val v = block(thisRef)
        map[w] = v
        return v
    }

    @Synchronized
    operator fun setValue(thisRef: THIS, property: KProperty<*>, value: VALUE) {
        map[WeakRef(thisRef)] = value
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


