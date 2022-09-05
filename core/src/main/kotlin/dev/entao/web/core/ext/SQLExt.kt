@file:Suppress("FunctionName")

package dev.entao.web.core.ext

import dev.entao.web.base.Prop
import dev.entao.web.base.Prop1
import dev.entao.web.base.Trim
import dev.entao.web.base.decodeAndCast
import dev.entao.web.base.returnClass
import dev.entao.web.base.setPropValue
import dev.entao.web.base.userName
import dev.entao.web.core.OnHttpContext
import dev.entao.web.sql.*
import kotlin.reflect.full.hasAnnotation


context(OnHttpContext)
fun OrmModel.fillProps(vararg ps: Prop) {
    fromRequest(ps.map { it.userName }.toSet())
}

context(OnHttpContext)
fun OrmModel.fromRequest(keySet: Set<String> = emptySet()) {
    val nameSet = context.paramMap.keys
    val thisModel = this
    thisModel::class._SQLProperties.forEach {
        val key = it.userName
        val b = if (keySet.isNotEmpty()) {
            key in nameSet && key in keySet
        } else {
            key in nameSet
        }
        if (b) {
            val sval = context[key]
            if (sval != null) {
                val v: String = if (it.hasAnnotation<Trim>()) {
                    sval.trim()
                } else {
                    sval
                }
                it.setPropValue(thisModel, it.decodeAndCast(v))
            }
        }
    }
}


//TODO bool
context (OnHttpContext)
val Prop.contextValue: Any?
    get() {
        return when (this.returnClass) {
            kotlin.Int::class -> context[this]?.toIntOrNull()
            kotlin.Long::class -> context[this]?.toLongOrNull()
            kotlin.Float::class, kotlin.Double::class -> context[this]?.toDoubleOrNull()
            kotlin.String::class -> {
                val s = context[this] ?: return null
                if (s.isNotEmpty()) {
                    return s
                } else null
            }

            else -> null
        }
    }


context (OnHttpContext)
fun equalProps(vararg ps: Prop): Where? {
    return ps.fold(null) { w: Where?, p: Prop ->
        w AND p.EQ
    }

}

context(OnHttpContext)
fun EQS(vararg ps: Prop): Where? {
    return ps.fold(null) { w: Where?, p ->
        w AND p.EQ
    }
}

context (OnHttpContext)
val Prop.EQ: Where?
    get() {
        val v = this.contextValue ?: return null
        return this EQ v
    }

// %value%
context(OnHttpContext)
val Prop1.LIKE_IN: Where?
    get() {
        val v = this.contextValue ?: return null
        if (v.toString().isEmpty()) {
            return null
        }
        return this LIKE """%$v%"""
    }

// value%
context(OnHttpContext)
val Prop1.LIKE_: Where?
    get() {
        val v = this.contextValue ?: return null
        if (v.toString().isEmpty()) {
            return null
        }
        return this LIKE """$v%"""
    }

// %value
context(OnHttpContext)
val Prop1._LIKE: Where?
    get() {
        val v = this.contextValue ?: return null
        if (v.toString().isEmpty()) {
            return null
        }
        return this LIKE """%$v"""
    }

context(OnHttpContext)
val Prop1.NE: Where?
    get() {
        val v = this.contextValue ?: return null
        return this NE v
    }

context(OnHttpContext)
val Prop1.GE: Where?
    get() {
        val v = this.contextValue ?: return null
        return this GE v
    }

context(OnHttpContext)
val Prop1.GT: Where?
    get() {
        val v = this.contextValue ?: return null
        return this GT v
    }

context(OnHttpContext)
val Prop1.LE: Where?
    get() {
        val v = this.contextValue ?: return null
        return this LE v
    }

context(OnHttpContext)
val Prop1.LT: Where?
    get() {
        val v = this.contextValue ?: return null
        return this LT v
    }


