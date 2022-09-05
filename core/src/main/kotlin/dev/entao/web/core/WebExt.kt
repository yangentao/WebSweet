@file:Suppress("unused")

package dev.entao.web.core

import dev.entao.web.base.Mimes
import dev.entao.web.base.matchIp4
import dev.entao.web.base.upperCased
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Part

/**
 * Created by entaoyang@163.com on 2017/4/4.
 */

val HttpServletRequest.currentUri: String get() = this.requestURI.trimEnd('/').lowercase()

val ServletRequest.method: String get() = (this as HttpServletRequest).method.upperCased

val ServletRequest.isGet: Boolean get() = "GET" == method

val ServletRequest.isPost: Boolean get() = "POST" == method

val ServletRequest.isHead: Boolean get() = "HEAD" == method

val ServletRequest.isPut: Boolean get() = "PUT" == method

val ServletRequest.isDelete: Boolean get() = "DELETE" == method

val ServletRequest.isOptions: Boolean get() = "OPTIONS" == method

val ServletRequest.isTrace: Boolean get() = "TRACE" == method

val ServletRequest.isMultipart: Boolean get() = "multipart/" in (this.contentType ?: "")


fun HttpServletRequest.header(name: String): String? {
    return this.getHeader(name)
}

val HttpServletRequest.referer: String?
    get() {
        return this.getHeader("Referer")
    }
val HttpServletRequest.userAgent: String?
    get() {
        return this.getHeader("User-Agent")
    }
val HttpServletRequest.accept: String?
    get() {
        return this.getHeader("Accept")
    }
val HttpServletRequest.acceptJson: Boolean
    get() {
        return accept?.contains("json") ?: false
    }


val Part.isFilePart: Boolean get() = this.submittedFileName != null && this.submittedFileName.isNotEmpty()

val Part.isString: Boolean
    get() {
        return this.submittedFileName == null
    }

val Part.stringValue: String?
    get() {
        return bytesValue?.toString(this.charset)
    }

val Part.bytesValue: ByteArray?
    get() {
        return this.inputStream.use { it.readBytes() }
    }

val Part.charset: Charset get() = charsetFromContentType(this.contentType) ?: Charsets.UTF_8

fun Part.writeTo(file: File): File {
    val os = FileOutputStream(file, false)
    this.inputStream.use {
        it.copyTo(os)
    }
    os.close()
    this.delete()
    return file
}

//text/plain;charset=utf-8  => UTF-8
fun charsetFromContentType(contentType: String?): Charset? {
    val ct = contentType ?: return null
    val a = ct.substringAfter(':', "").substringAfter('=', "").trim()
    if (a.isEmpty()) {
        return null
    }
    return Charset.forName(a)
}


fun HttpServletResponse.contentTypeJSON() {
    this.contentType = Mimes.JSON
}

fun HttpServletResponse.contentTypeHtml() {
    this.contentType = Mimes.HTML
}

fun HttpServletResponse.contentTypeXML() {
    this.contentType = Mimes.XML
}

fun HttpServletResponse.contentTypeStream() {
    this.contentType = Mimes.STREAM
}

val HttpServletRequest.clientIp: String
    get() {
        val a = header("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        if (a != null && a.matchIp4()) {
            return a
        }
        val b = header("X-Real-IP")?.trim()
        if (b != null && b.matchIp4()) {
            return b
        }
        return this.remoteAddr
    }


