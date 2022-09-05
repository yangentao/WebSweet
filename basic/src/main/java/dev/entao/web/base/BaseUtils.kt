@file:Suppress("unused")

package dev.entao.web.base

import java.io.File
import java.text.Collator
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.random.Random
import kotlin.reflect.KClass


const val KB: Int = 1024
const val MB: Int = 1024 * 1024
const val GB: Int = 1024 * 1024 * 1024


inline fun safe(block: () -> Unit) {
    try {
        block()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun tempFileName(ext: String = ""): String {
    return if (ext.isEmpty() || ext.startsWith('.')) {
        UUID.randomUUID().hexText + ext
    } else {
        UUID.randomUUID().hexText + "." + ext
    }
}

val UUID.hexText: String get() = String.format("%x%x", this.mostSignificantBits, this.leastSignificantBits)

fun dateDisplay(v: Any, format: String): String {
    //java.util.Date包含java.sql.Date和Timestamp,Time
    return when (v) {
        is java.util.Date -> SimpleDateFormat(format, Locale.getDefault()).format(v)
        is Long -> SimpleDateFormat(format, Locale.getDefault()).format(java.util.Date(v))
        is LocalDate -> v.format(format)
        is LocalDateTime -> v.format(format)
        is LocalTime -> v.format(format)
        else -> v.toString()
    }
}


val Int.fileSize: String get() = this.toLong().fileSize

val Long.fileSize: String
    get() {
        return when {
            this > GB -> (this * 1.0 / GB).toString().keep(2) + "G"
            this > MB -> (this * 1.0 / MB).toString().keep(2) + "M"
            this > KB -> (this * 1.0 / KB).toString().keep(2) + "K"
            else -> this.toString() + "字节"
        }
    }

// "1.2345".keep(2) => "1.23"
fun String.keep(n: Int): String {
    val idx = this.indexOf('.')
    if (idx < 0 || idx + n + 1 >= this.length) {
        return this
    }
    return this.substring(0, idx + n + 1)
}


//fun main() {
//    println(joinPath("/a/", "b/", "/c", "/d/", "e", "/", ""))
//    println(joinPath("c:\\", "b/", "/c", "/d/", "e", "/", ""))
//}
object Rand {
    val r = Random(System.currentTimeMillis())

    fun nextInt(from: Int, to: Int): Int {
        return r.nextInt(from, to)
    }
}

fun Double.formatDot(n: Int): String {
    return if (n > 0) String.format("%.${n}f", this) else this.toInt().toString()
}

fun joinPath(path: String, vararg ps: String): String {
    var sb = path
    for (p in ps) {
        sb = sb.trimEnd('/', '\\')
        sb += File.separatorChar
        sb += p.trimStart('/', '\\')
    }
    return sb
}

fun joinURL(path: String, vararg ps: String): String {
    var sb = path
    for (p in ps) {
        sb = sb.trimEnd('/')
        sb += "/"
        sb += p.trimStart('/')
    }
    return sb
}

fun KClass<*>.resourceBytes(name: String): ByteArray? {
    val i = this.java.classLoader.getResourceAsStream(name) ?: return null
    i.use {
        return it.readBytes()
    }
}

fun KClass<*>.resourceText(name: String): String? {
    val i = this.java.classLoader.getResourceAsStream(name) ?: return null
    i.use {
        return it.readBytes().toString(Charsets.UTF_8)
    }
}


fun File.ensureDirs(): File {
    if (!this.exists()) {
        this.mkdirs()
    }
    return this
}

fun printX(vararg vs: Any?) {
    val s = vs.joinToString(" ") {
        it?.toString() ?: "null"
    }
    println(s)
}

val chinaCollator: Collator by lazy {
    Collator.getInstance(Locale.CHINESE)
}

class ChinaComparator<T>(val block: (T) -> String?) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int {
        if (o1 === o2) {
            return 0
        }
        if (o1 == null) {
            return -1
        }
        if (o2 == null) {
            return 1
        }
        val s1 = block(o1)
        val s2 = block(o2)
        if (s1 == s2) {
            return 0
        }
        if (s1 == null) {
            return -1
        }
        if (s2 == null) {
            return 1
        }
        return chinaCollator.compare(s1, s2)
    }

}

inline fun <T : AutoCloseable?, R> T.useX(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this?.close()
        } catch (closeException: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            this?.close()
        }
    }
}

fun MutableList<String>.chinaSort() {
    this.sortWith(chinaCollator)
}

fun Iterable<String>.chinaSorted(): List<String> {
    return this.sortedWith(chinaCollator)
}

fun <T> MutableList<T>.chinaSortBy(block: (T) -> String?) {
    val cmp = ChinaComparator<T>(block)
    this.sortWith(cmp)
}

fun <T> Iterable<T>.chinaSortedBy(block: (T) -> String?): List<T> {
    val cmp = ChinaComparator<T>(block)
    return this.sortedWith(cmp)
}

infix fun String?.emp(other: String): String {
    if (this == null || this.isEmpty())
        return other
    else
        return this
}




