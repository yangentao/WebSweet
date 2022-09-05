package dev.entao.web.log

import dev.entao.web.json.YsonValue
import java.io.PrintWriter
import java.io.StringWriter

fun anyArrayToString(args: Array<out Any?>): String {
    return args.joinToString(" ") {
        anyToString(it)
    }
}

fun anyToString(obj: Any?): String {
    if (obj == null) {
        return "null"
    }
    if (obj is String) {
        return obj
    }
    if (obj.javaClass.isPrimitive) {
        return obj.toString()
    }

    if (obj is Throwable) {
        val sw = StringWriter(512)
        val pw = PrintWriter(sw)
        obj.printStackTrace(pw)
        return sw.toString()
    }
    if (obj is YsonValue) return obj.toString()

    if (obj is Array<*>) {
        val s = obj.joinToString(",") { anyToString(it) }
        return "ARRAY[$s]"
    }
    if (obj is List<*>) {
        val s = obj.joinToString(", ") { anyToString(it) }
        return "LIST[$s]"
    }
    if (obj is Map<*, *>) {
        val s = obj.map { "${anyToString(it.key)} = ${anyToString(it.value)}" }.joinToString(",")
        return "MAP{$s}"
    }
    if (obj is Iterable<*>) {
        val s = obj.joinToString(", ") { anyToString(it) }
        return "ITERABLE[$s]"
    }
    return obj.toString()
}