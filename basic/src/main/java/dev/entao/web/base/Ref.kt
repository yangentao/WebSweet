package dev.entao.web.base

import kotlin.jvm.internal.CallableReference
import kotlin.jvm.internal.FunctionReference
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod


fun KParameter.acceptValue(value: Any): Boolean {
    return value::class.isSubclassOf(this.type.classifier as KClass<*>)
}

fun KParameter.acceptClass(cls: KClass<*>): Boolean {
    return cls.isSubclassOf(this.type.classifier as KClass<*>)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<*>.createInstanceX(vararg ps: Any): T {
    val c = this.constructors.firstOrNull {
        var result = true
        val vps = it.valueParams
        if (vps.size == ps.size) {
            for (i in vps.indices) {
                if (!vps[i].acceptValue(ps[i])) {
                    result = false
                    break
                }
            }
        } else {
            result = false
        }
        result
    } ?: error("No constructor fit $this, args: ${ps.joinToString(",") { it.toString() }}")

    return c.call(*ps) as T
}

val KFunction<*>.paramNames: List<String>
    get() {
        return this.parameters.filter { it.kind == KParameter.Kind.VALUE }.map { it.userName }
    }
val KFunction<*>.valueParams: List<KParameter>
    get() {
        return this.parameters.filter { it.kind == KParameter.Kind.VALUE }
    }


val KType.genericArgs: List<KTypeProjection> get() = this.arguments.filter { it.variance == KVariance.INVARIANT }
val KType.isGeneric: Boolean get() = this.arguments.isNotEmpty()


val KFunction<*>.ownerClass: KClass<*>?
    get() {
        if (this is FunctionReference) {
            if (this.boundReceiver != CallableReference.NO_RECEIVER) {
                return this.boundReceiver::class
            }
            val c = this.owner as? KClass<*>
            if (c != null) {
                return c
            }
        } else {
            return this.javaMethod?.declaringClass?.kotlin
        }
        return null
    }
val KFunction<*>.ownerObject: Any?
    get() {
        if (this is FunctionReference) {
            if (this.boundReceiver != CallableReference.NO_RECEIVER) {
                return this.boundReceiver
            }
        }
        return null
    }

val KFunction<*>.firstParamName: String?
    get() {
        return this.valueParameters.firstOrNull()?.userName
    }


val KProperty<*>.ownerClass: KClass<*>?
    get() {
        if (this is CallableReference) {
            if (this.boundReceiver != CallableReference.NO_RECEIVER) {
                return this.boundReceiver::class
            }
            val c = this.owner as? KClass<*>
            if (c != null) {
                return c
            }
        } else {
            return this.javaField?.declaringClass?.kotlin
        }

        return null
    }

val KProperty<*>.ownerObject: Any?
    get() {
        if (this is CallableReference) {
            if (this.boundReceiver != CallableReference.NO_RECEIVER) {
                return this.boundReceiver::class
            }
        }
        return null
    }


val KProperty<*>.returnClass: KClass<*> get() = this.returnType.classifier as KClass<*>


fun KProperty<*>.getPropValue(inst: Any? = null): Any? {
    if (this.getter.parameters.isEmpty()) {
        return this.getter.call()
    }
    return this.getter.call(inst)
}


fun KMutableProperty<*>.setPropValue(inst: Any, value: Any?) {
    try {
        this.setter.call(inst, value)
    } catch (t: Throwable) {
        println("setValue Error: " + this.userName + "." + this.name + ", value=" + value?.toString())
        t.printStackTrace()
        throw t
    }
}

val KProperty<*>.isPublic: Boolean get() = this.visibility == KVisibility.PUBLIC
val KFunction<*>.isPublic: Boolean get() = this.visibility == KVisibility.PUBLIC
