package dev.entao.web.core

import dev.entao.web.base.ClassProps
import dev.entao.web.base.FunProps
import dev.entao.web.base.Name
import dev.entao.web.base.lowerCased
import dev.entao.web.base.substr
import dev.entao.web.base.userName
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

/**
 * Created by entaoyang@163.com on 2017/5/7.
 */

val KFunction<*>.actionName: String by FunProps { f ->
    val ac = f.findAnnotation<Action>() ?: error("$f 没有Action注解")
    when {
        ac.index -> ""
        ac.rename.isNotEmpty() -> ac.rename
        else -> f.userName.lowerCased
    }
}


val KClass<*>.pageName: String by ClassProps { makePageName(it) }



private fun makePageName(cls: KClass<*>): String {
    val c = cls.findAnnotation<Controller>()
    if (c != null) {
        if (c.index) {
            return ""
        }
        if (c.rename.isNotEmpty()) {
            return c.rename
        }
    }

    val named = cls.findAnnotation<Name>()?.value
    if (named != null) {
        return named.lowercase(Locale.getDefault())
    }
    val clsName = cls.simpleName!!
    for (ps in HttpService.controllerSuffixs) {
        if (clsName != ps && clsName.endsWith(ps)) {
            return clsName.substr(0, clsName.length - ps.length).lowerCased
        }
    }
    return clsName.lowercase(Locale.getDefault())
}


val String.intList: List<Int>
    get() {
        return this.split(',').map { it.toInt() }
    }

fun isSubpath(longPath: String, shortPath: String): Boolean {
    val uu = "$longPath/"
    return if (shortPath.endsWith("/")) {
        uu.startsWith(shortPath)
    } else {
        uu.startsWith("$shortPath/")
    }
}

fun buildPath(vararg ps: String): String {
    return "/" + ps.filter { it.isNotEmpty() }.joinToString("/") { it.trim('/') }

}

val Throwable.realReason: Throwable
    get() {
        val c = this.cause
        if (c != null) {
            return c.realReason
        }
        if (this is InvocationTargetException) {
            if (this.targetException != null) {
                return this.targetException.realReason
            }
        }
        return this
    }

val String.withSep: String
    get() {
        if (this.endsWith('/')) return this
        return "$this/"
    }