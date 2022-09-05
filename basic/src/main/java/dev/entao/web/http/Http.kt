@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName", "PropertyName", "FunctionName")

package dev.entao.web.http

import dev.entao.web.base.*
import dev.entao.web.json.YsonObject
import dev.entao.web.log.logd
import dev.entao.web.log.loge
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL
import java.util.*
import java.util.zip.GZIPInputStream

/**
 * Created by entaoyang@163.com on 2016/12/20.
 */
fun httpGet(url: String, block: HttpGet.() -> Unit): HttpResult {
    val h = HttpGet(url)
    h.block()
    return h.request()
}

fun httpPost(url: String, block: HttpPost.() -> Unit): HttpResult {
    val h = HttpPost(url)
    h.block()
    return h.request()
}

fun httpRaw(url: String, block: HttpRaw.() -> Unit): HttpResult {
    val h = HttpRaw(url)
    h.block()
    return h.request()
}

fun httpMultipart(url: String, block: HttpMultipart.() -> Unit): HttpResult {
    val h = HttpMultipart(url)
    h.block()
    return h.request()
}


class HttpGet(url: String) : HttpReq(url, "GET") {

    override fun onSend(connection: HttpURLConnection) {
    }
}

class HttpPost(url: String) : HttpReq(url, "POST") {

    init {
        headers.contentType = "application/x-www-form-urlencoded;charset=utf-8"
    }

    override fun onSend(connection: HttpURLConnection) {
        val os = connection.outputStream
        try {
            val s = buildArgs()
            if (s.isNotEmpty()) {
                write(os, s)
                if (dumpReq) {
                    logd("--body:", s)
                }
            }
            os.flush()
        } finally {
            os.closeSafe()
        }
    }
}

class HttpRaw(url: String) : HttpReq(url, "POST") {
    private lateinit var rawData: ByteArray


    fun data(contentType: String, data: ByteArray): HttpRaw {
        headers.contentType = contentType
        this.rawData = data
        return this
    }

    fun jsonObject(block: YsonObject.() -> Unit): HttpRaw {
        val yo = YsonObject()
        yo.block()
        return this.json(yo.toString())
    }

    fun json(json: String): HttpRaw {
        return data("application/json;charset=utf-8", json.toByteArray(charsetUTF8))
    }

    fun xml(xml: String): HttpRaw {
        return data("application/xml;charset=utf-8", xml.toByteArray(charsetUTF8))
    }

    override fun onSend(connection: HttpURLConnection) {
        val os = connection.outputStream
        try {
            os.write(rawData)
            if (dumpReq && allowDump(headers.contentType)) {
                logd("--body:", String(rawData, Charsets.UTF_8))
            }
            os.flush()
        } finally {
            os.closeSafe()
        }
    }
}

class HttpMultipart(url: String) : HttpReq(url, "POST") {
    private val BOUNDARY = UUID.randomUUID().hexText

    private val fileList = ArrayList<FileParam>()

    init {
        headers.contentType = "multipart/form-data; boundary=$BOUNDARY"
    }

    fun file(fileParam: FileParam): HttpMultipart {
        fileList.add(fileParam)
        return this
    }

    fun file(key: String, file: File): HttpMultipart {
        val p = FileParam(key, file)
        return file(p)
    }


    fun file(key: String, file: File, block: FileParam.() -> Unit): HttpMultipart {
        val p = FileParam(key, file)
        p.block()
        return file(p)
    }

    override fun onSend(connection: HttpURLConnection) {
        connection.outputStream.use {
            sendMultipart(it)
            it.flush()
        }
    }

    override fun dumpReq() {
        super.dumpReq()
        for (fp in fileList) {
            logd("--file:", fp)
        }
    }

    override fun preConnect(connection: HttpURLConnection) {
        super.preConnect(connection)
        if (fileList.size > 0) {
            val os = SizeStream()
            sendMultipart(os)
            connection.setFixedLengthStreamingMode(os.size)
        }
    }

    @Throws(IOException::class)
    private fun sendMultipart(os: OutputStream) {
        if (allArgs.size > 0) {
            for (e in allArgs.entries) {
                writeln(os, "--", BOUNDARY)
                writeln(os, "Content-Disposition: form-data; name=\"${e.key}\"")
                writeln(os, "Content-Type:text/plain;charset=utf-8")
                writeln(os)
                writeln(os, e.value)
            }
        }
        if (fileList.size > 0) {
            for (fp in fileList) {
                writeln(os, "--", BOUNDARY)
                writeln(os, "Content-Disposition:form-data;name=\"${fp.key}\";filename=\"${fp.filename}\"")
                writeln(os, "Content-Type:${fp.mime}")
                writeln(os, "Content-Transfer-Encoding: binary")
                writeln(os)
                val total = fp.file.length().toInt()
                if (os is SizeStream) {
                    os.incSize(total)
                } else {
                    copyStream(FileInputStream(fp.file), true, os, false, total, fp.progress)
                }
                writeln(os)
            }
        }
        writeln(os, "--", BOUNDARY, "--")
    }
}


abstract class HttpReq(val url: String, val method: String = "GET") {

    val UTF8 = "UTF-8"
    val charsetUTF8 = Charsets.UTF_8
    val headers: HttpHeaders = HttpHeaders()
    protected val allArgs = LinkedHashMap<String, String>()


    private var timeoutConnect = 10000
    private var timeoutRead = 10000
    //	private var rawData: ByteArray? = null


    var saveToFile: File? = null
    var progress: Progress? = null

    var dumpReq: Boolean = false
    var dumpResp: Boolean = false

    init {
        headers.userAgent = "sweet http client"
        headers.accept = "application/json,text/plain,text/html,*/*"
        headers.acceptCharset = "UTF-8,*"
        headers.connection = "close"
    }


    infix fun String.arg(v: String) {
        arg(this, v)
    }

    infix fun String.arg(v: Number) {
        arg(this, v)
    }

    infix fun String.arg(v: Boolean) {
        arg(this, v)
    }

    fun arg(key: String, value: Any): HttpReq {
        allArgs[key] = value.toString()
        return this
    }


    fun args(vararg args: Pair<String, String>): HttpReq {
        for ((k, v) in args) {
            allArgs[k] = v
        }
        return this
    }

    fun args(map: Map<String, String>): HttpReq {
        allArgs.putAll(map)
        return this
    }


    protected fun buildArgs(): String {
        return allArgs.map {
            it.key.urlEncoded + "=" + it.value.urlEncoded
        }.joinToString("&")
    }

    @Throws(MalformedURLException::class)
    fun buildGetUrl(): String {
        val sArgs = buildArgs()
        if (sArgs.isEmpty()) return url
        val sb = StringBuilder(url.length + sArgs.length + 8)
        sb.append(url)
        if ('?' !in sb) {
            sb.append('?')
        }
        if (sb.last() != '?') {
            sb.append('&')
        }
        sb.append(sArgs)
        return sb.toString()
    }

    open fun dumpReq() {
        if (!dumpReq) {
            return
        }
        logd("Http Request:", url)
        for ((k, v) in headers.allMap) {
            logd("--head:", k, "=", v)
        }
        for ((k, v) in allArgs) {
            logd("--arg:", k, "=", v)
        }
    }

    @Throws(IOException::class)
    fun write(os: OutputStream, vararg arr: String) {
        for (s in arr) {
            os.write(s.toByteArray(Charsets.UTF_8))
        }
    }

    @Throws(IOException::class)
    fun writeln(os: OutputStream, vararg arr: String) {
        for (s in arr) {
            os.write(s.toByteArray(Charsets.UTF_8))
        }
        os.write("\r\n".toByteArray(Charsets.UTF_8))
    }

    @Throws(ProtocolException::class, UnsupportedEncodingException::class)
    protected open fun preConnect(connection: HttpURLConnection) {
        HttpURLConnection.setFollowRedirects(true)
        connection.doOutput = method != "GET"
        connection.doInput = true
        connection.connectTimeout = timeoutConnect
        connection.readTimeout = timeoutRead
        connection.requestMethod = method
        connection.useCaches = false

        for (e in headers.allMap.entries) {
            connection.setRequestProperty(e.key, e.value)
        }
    }

    @Throws(IOException::class)
    private fun onResponse(connection: HttpURLConnection): HttpResult {
        val result = HttpResult(this.url).apply {
            code = connection.responseCode
            msg = connection.responseMessage
            contentType = connection.contentType
            headers = connection.headerFields
            contentLength = connection.contentLength
        }
        val total = connection.contentLength
        try {
            val saveFile = this.saveToFile
            saveFile?.also {
                val dir = it.parentFile
                if (dir != null) {
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            loge("创建目录失败")
                            throw IOException("创建目录失败!")
                        }
                    }
                }
            }
            val os: OutputStream = if (saveFile != null) {
                FileOutputStream(saveFile)
            } else {
                ByteArrayOutputStream(if (total > 0) total else 64)
            }

            var input = connection.inputStream
            val mayGzip = connection.contentEncoding
            if (mayGzip != null && mayGzip.contains("gzip")) {
                input = GZIPInputStream(input)
            }
            copyStream(input, true, os, true, total, progress)
            if (os is ByteArrayOutputStream) {
                result.buffer = os.toByteArray()
            }
        } catch (ex: Exception) {
            result.exception = ex
        }
        return result
    }

    @Throws(IOException::class)
    protected abstract fun onSend(connection: HttpURLConnection)


    fun request(): HttpResult {
        var connection: HttpURLConnection? = null
        try {
            dumpReq()
            connection = if (this is HttpGet || this is HttpRaw) {
                URL(buildGetUrl()).openConnection() as HttpURLConnection
            } else {
                URL(url).openConnection() as HttpURLConnection
            }

            preConnect(connection)
            connection.connect()
            onSend(connection)
            val r = onResponse(connection)
            if (dumpResp) {
                r.dump()
            }
            return r
        } catch (ex: Exception) {
            ex.printStackTrace()
            loge(ex)
            val result = HttpResult(this.url)
            result.exception = ex
            return result
        } finally {
            connection?.disconnect()
        }
    }

    fun download(saveto: File, progress: Progress?): HttpResult {
        this.saveToFile = saveto
        this.progress = progress
        return request()
    }
}


fun allowDump(ct: String?): Boolean {
    val a = ct?.lowerCased ?: return false
    return "json" in a || "xml" in a || "html" in a || "text" in a
}


//fun main() {
//	val url = "http://localhost:8080/taoke/userapi/login"
//	val h = HttpGet(url)
//	h.dumpReq = true
//	h.dumpResp = true
//	h.arg("user", "yang")
//	h.arg("pwd", "entao")
//	val r = h.request()
//	logd(r.strUtf8())
//	logd("END")
//}
