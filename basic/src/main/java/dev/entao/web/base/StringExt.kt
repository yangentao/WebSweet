package dev.entao.web.base

import java.net.URLDecoder
import java.net.URLEncoder
import java.text.DecimalFormat
import java.util.*

/**
 * Created by entaoyang@163.com on 16/5/13.
 */
//数字a-zA-Z和_
val REG_ID = "\\w+"

//11位数字, 1开头
val REG_PHONE = "1\\d{10}"

//abc.xyz.123@456.opq.com.cn
val REG_EMAIL = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+)+)$"

//0-9
val REG_INTEGER = "\\d+"

//0-9和.
val REG_FLOAT = "[.\\d]+"


infix operator fun String.times(n: Int): String {
    val sb = StringBuilder(this.length * n + 1)
    for (i in 0 until n) {
        sb.append(this)
    }
    return sb.toString()
}

fun Float.maxFraction(n: Int): String {
    return this.toDouble().maxFraction(n)
}

fun Double.maxFraction(n: Int): String {
    if (n <= 0) {
        return this.toLong().toString()
    }
    val f = DecimalFormat()
    f.maximumFractionDigits = n
    f.isGroupingUsed = false
    return f.format(this)
}

fun String.matchIp4(): Boolean {
    return this.matches("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""".toRegex())
}

fun String.replaceChars(vararg charValuePair: Pair<Char, String>): String {
    val sb = StringBuilder(this.length + 8)
    for (ch in this) {
        val p = charValuePair.find { it.first == ch }
        if (p != null) {
            sb.append(p.second)
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

//"""
//		fun ok(){
//			print("hello");
//		}
//"""
//==>
//"""fun ok(){
//	print("hello");
//}"""
fun String.trimColumns(tabSize: Int = 4): String {
    val lines = this.lines()
    var n = 1000
    for (line in lines) {
        if (line.trim().isEmpty()) {
            continue
        }
        var w = 0
        for (c in line) {
            if (c == ' ') {
                w += 1
            } else if (c == '\t') {
                w += tabSize
            } else {
                break
            }
        }
        n = kotlin.math.min(n, w)
    }
    val ls = ArrayList<String>()
    for (line in lines) {
        var w = 0
        var index = 0
        for (c in line) {
            if (w >= n) {
                break
            }
            ++index
            if (c == ' ') {
                w += 1
            } else if (c == '\t') {
                w += tabSize
            } else {
                break
            }
        }
        ls += line.substring(index)
    }
    return ls.joinToString("\n")
}

val String.urlEncoded: String
    get() {
        return URLEncoder.encode(this, Charsets.UTF_8.name())
    }
val String.urlDecoded: String
    get() {
        return URLDecoder.decode(this, Charsets.UTF_8.name())
    }

val String.base64Decoded: String
    get() {
        if (this.isEmpty()) {
            return ""
        }
        val ba = Base64.getUrlDecoder().decode(this)
        return String(ba, Charsets.UTF_8)
    }
val String.base64Encoded: String
    get() {
        if (this.isEmpty()) {
            return ""
        }
        return Base64.getUrlEncoder().encodeToString(this.toByteArray())
    }

fun String.substr(from: Int, size: Int): String {
    val a = if (from >= 0) {
        from
    } else 0
    val b = if (a + size < this.length) {
        a + size
    } else {
        this.length
    }
    return this.substring(a, b)
}

//"abcd=defg-123".substringBetween('=','-') => "defg"
//"abcd=defg=123".substringBetween('=','=') => "defg"
//"abcd==123".substringBetween('=','=') => ""
//"abcd=123".substringBetween('=','=') => null
fun String.substringBetween(a: Char, b: Char): String? {
    val nA = this.indexOf(a)
    if (nA >= 0) {
        val nB = this.indexOf(b, nA + 1)
        if (nB >= 0) {
            return this.substring(nA + 1, nB)
        }
    }
    return null
}

fun String?.empty(): Boolean {
    return this == null || this.isEmpty()
}

fun String?.notEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

fun String?.emptyOr(s: String): String {
    return if (this == null || this.isEmpty()) s else this
}

fun String?.nullOr(s: String): String {
    return this ?: s
}

fun String?.hasCharLast(ch: Char): Boolean {
    return (this?.lastIndexOf(ch) ?: -1) >= 0
}

fun String?.hasChar(ch: Char): Boolean {
    return (this?.indexOf(ch) ?: -1) >= 0
}

fun String.head(n: Int): String {
    if (n <= 0) {
        return ""
    }
    if (this.length <= n) {
        return this
    }
    return this.substring(0, n)
}

fun String.tail(n: Int): String {
    if (n <= 0) {
        return ""
    }
    if (this.length < n) {
        return this
    }
    return this.substring(this.length - n)
}

//分隔成长度不大于n的字符串数组
fun String.truck(n: Int): List<String> {
    val ls = ArrayList<String>()
    if (this.length <= n) {
        ls.add(this)
    } else {
        val x = this.length / n
        val y = this.length % n
        for (i in 1..x) {
            val start = (i - 1) * n
            ls.add(this.substring(start, start + n))
        }
        if (y != 0) {
            ls.add(this.substring(x * n))
        }
    }
    return ls
}

fun String.escapeHtml(): String {
    val sb = StringBuffer((this.length * 1.1).toInt())
    this.forEach {
        when (it) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '"' -> sb.append("&quot;")
            '\'' -> sb.append("&#x27;")
            '&' -> sb.append("&amp;")
            '/' -> sb.append("&#x2F;")
            else -> sb.append(it)
        }
    }
    return sb.toString()
}

fun String.escapeHtml(forView: Boolean): String {
    if (!forView) {
        return this.escapeHtml()
    }
    val sb = StringBuffer((this.length * 1.1).toInt())
    var i = 0
    val CR = 13.toChar()
    val LF = 10.toChar()
    val SP = ' '
    val BR = "<br/>"
    while (i < this.length) {
        val c = this[i]
        when (c) {
            '<' -> sb.append("&lt;")
            '>' -> sb.append("&gt;")
            '"' -> sb.append("&quot;")
            '\'' -> sb.append("&#x27;")
            '&' -> sb.append("&amp;")
            '/' -> sb.append("&#x2F;")
            SP -> {
                sb.append("&nbsp;")
            }
            CR -> {
                val nextChar: Char? = if (i + 1 < this.length) this[i + 1] else null
                if (nextChar != LF) {
                    sb.append(BR)
                }
            }
            LF -> {
                sb.append(BR)
            }
            else -> sb.append(c)
        }
        ++i
    }

    return sb.toString()
}

val String.lowerCased: String
    get() {
        return this.lowercase(Locale.getDefault())
    }
val String.upperCased: String
    get() {
        return this.uppercase(Locale.getDefault())
    }
