@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.entao.web.core

import dev.entao.web.base.Mimes
import dev.entao.web.base.hexText
import dev.entao.web.base.paramNames
import dev.entao.web.base.urlEncoded
import dev.entao.web.base.userName
import dev.entao.web.log.logd
import java.io.File
import java.io.PrintWriter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.Part
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty

/**
 * Created by entaoyang@163.com on 2016/12/18.
 */

class ExtValue {
    inline operator fun <reified T : Any> getValue(thisRef: HttpContext, property: KProperty<*>): T? {
        return thisRef.getExtValue(property.userName)
    }

    inline operator fun <reified T : Any> setValue(thisRef: HttpContext, property: KProperty<*>, value: T?) {
        thisRef.setExtValue(property.userName, value)
    }

}

class ExtStringValue {
    operator fun getValue(thisRef: HttpContext, property: KProperty<*>): String {
        return thisRef.getExtValue<String>(property.userName) ?: ""
    }

    operator fun setValue(thisRef: HttpContext, property: KProperty<*>, value: String) {
        thisRef.setExtValue(property.userName, value)
    }
}

class ExtIntValue {
    operator fun getValue(thisRef: HttpContext, property: KProperty<*>): Int {
        return thisRef.getExtValue<Int>(property.userName) ?: 0
    }

    operator fun setValue(thisRef: HttpContext, property: KProperty<*>, value: Int) {
        thisRef.setExtValue(property.userName, value)
    }
}

class ExtLongValue {
    operator fun getValue(thisRef: HttpContext, property: KProperty<*>): Long {
        return thisRef.getExtValue<Long>(property.userName) ?: 0L
    }

    operator fun setValue(thisRef: HttpContext, property: KProperty<*>, value: Long) {
        thisRef.setExtValue(property.userName, value)
    }
}

class HttpContext(val httpService: HttpService, val request: HttpServletRequest, val response: HttpServletResponse, val chain: FilterChain) {
    val app: BaseApp get() = httpService.app
    val currentUri: String get() = request.currentUri

    val extMap = HashMap<String, Any>(16)

    val paramMap: LinkedHashMap<String, List<String>> = LinkedHashMap()
    val partList: List<Part>

    val hostUrl: String by lazy { request.scheme + "://" + request.getHeader("host") }

    // ==> /admin
    val contextPath: String by lazy { this.request.contextPath }

    val appUrl: String by lazy { hostUrl + contextPath }

    val isCommited: Boolean get() = this.response.isCommitted

    //可以根据这个属性来判断:  当发生错误的时候是否返回HTML格式
    val hasReferer: Boolean get() = this.request.referer != null
    val tempPartFiles = ArrayList<File>()


    init {
        for ((k, v) in this.request.parameterMap) {
            paramMap[k] = v.toList()
        }
        partList = if (request.isMultipart) {
            request.parts.toList()
        } else emptyList()
    }

    fun commitNeed() {
        if (!this.isCommited) {
            this.response.flushBuffer()
        }
    }

    //uri:  /app/page/action  => page/action
    val pathInApp: String
        get() {
            if (contextPath.isEmpty()) return currentUri.trimStart('/')
            return currentUri.substringAfter(contextPath, "").trimStart('/')
        }

    // page/action => action
    val pathInController: String
        get() {
            return pathInApp.substringAfter('/', "")
        }
//    val pathInController:String get() {
//        val uri = request.currentUri
//
//    }

    fun <V : Any> setExtValue(key: String, value: V?) {
        if (value == null) {
            extMap.remove(key)
        } else {
            extMap[key] = value
        }
    }

    inline fun <reified V : Any> getExtValue(key: String): V? {
        return extMap[key] as? V
    }


    fun deleteTempPartFiles() {
        for (f in tempPartFiles) {
            f.delete()
        }
    }

    fun createTempPartFile(): File {
        val f = File(app.tempDir, UUID.randomUUID().hexText)
        tempPartFiles += f
        return f
    }

    fun hasParam(key: String): Boolean {
        return paramMap.containsKey(key) || partList.any { it.name == key }
    }

    operator fun get(parameter: KParameter): String? {
        return paramMap[parameter.userName]?.firstOrNull()
    }

    operator fun get(property: KProperty<*>): String? {
        return paramMap[property.userName]?.firstOrNull()
    }

    operator fun get(key: String): String? {
        return paramMap[key]?.firstOrNull()
    }

    fun param(key: String): String? {
        return paramMap[key]?.firstOrNull()
    }

    fun clean() {

    }

    fun urlOf(str: String): String {
        val idx = str.indexOf("://")
        if (idx in 1..6) {
            return str
        }
        if (str.startsWith("/")) {
            return hostUrl + str
        }
        if (str.startsWith("@/")) {
            return appUrl + str.substring(1)
        }
        return "$appUrl/$str"
    }

    fun uriOf(str: String): String {
        val idx = str.indexOf("://")
        if (idx >= 0) {
            val s = str.substringAfter("://").substringAfter("/", "")
            if (s.isEmpty()) return "/"
            return s
        }
        if (str.startsWith("/")) {
            return str
        }
        if (str.startsWith("@/")) {
            return contextPath + str.substring(1)
        }
        return "$contextPath/$str"
    }

    fun uriAction(action: HttpAction): String {
        return this.httpService.uriOf(action) ?: error("No Action Found $action ")
    }

    fun uriActionValues(action: HttpAction, vararg vs: Any): String {
        val ls = ArrayList<String>()
        val nameList = action.paramNames
        val n = Integer.min(vs.size, nameList.size)
        for (i in 0 until n) {
            ls += nameList[i].urlEncoded + "=" + vs[i].toString().urlEncoded
        }
        if (ls.isEmpty()) {
            return uriAction(action)
        }
        return uriAction(action) + "?" + ls.joinToString("&")
    }

    fun uriActionKeyValues(action: HttpAction, pairList: List<Pair<String, Any>>): String {
        val ls = ArrayList<String>()
        for (p in pairList) {
            ls += p.first.urlEncoded + "=" + p.second.toString().urlEncoded
        }
        if (ls.isEmpty()) {
            return uriAction(action)
        }
        return uriAction(action) + "?" + ls.joinToString("&")
    }

    fun uriActionParams(action: HttpAction, removeKeys: Set<String>, appendKV: List<Pair<String, String>>): String {
        val ls = ArrayList<Pair<String, String>>()
        for ((k, v) in paramMap) {
            if (k !in removeKeys) {
                for (s in v) {
                    ls += k to s
                }
            }
        }
        ls.addAll(appendKV)
        return uriActionKeyValues(action, ls)
    }

    val acceptJson: Boolean
        get() {
            return request.acceptJson
        }
    val acceptHtml: Boolean
        get() {
            return Mimes.HTML in (request.header("Accept") ?: "")
        }

    fun redirect(url: String) {
        response.sendRedirect(url)
    }

    fun redirect(action: HttpAction, vararg ps: Pair<String, Any>) {
        val uri = uriActionKeyValues(action, ps.toList())
        response.sendRedirect(uri)
    }

    fun redirect(url: String, queryString: String) {
        if (queryString.isEmpty()) {
            response.sendRedirect(url)
            return
        }
        val a = url.lastIndexOf('?')
        if (a < 0) {
            response.sendRedirect("$url?$queryString")
        } else if (a == url.lastIndex) {
            response.sendRedirect("$url$queryString")
        } else {
            response.sendRedirect("$url&$queryString")
        }
    }

    fun header(key: String, value: String) {
        response.addHeader(key, value)
    }

    fun headerDate(key: String, value: Long) {
        response.setDateHeader(key, value)
    }

    fun header(key: String): String? {
        return request.getHeader(key)
    }

    fun headerDate(key: String): Long {
        return request.getDateHeader(key)
    }

    fun getSession(key: String): String? {
        val se: HttpSession = request.getSession(false) ?: return null
        return se.getAttribute(key) as? String
    }

    fun putSession(key: String, value: String) {
        val se: HttpSession = request.getSession(true)
        se.setAttribute(key, value)
        se.maxInactiveInterval = app.sessionTimeoutSeconds

    }

    fun removeSession(key: String) {
        val se: HttpSession = request.getSession(false) ?: return
        se.removeAttribute(key)
    }

    fun sendError(code: Int, msg: String = "") {
        if (msg.isEmpty()) {
            response.sendError(code)
        } else {
            response.sendError(code, msg)
        }
    }

    fun send(mimes: String, data: String) {
        this.response.contentType = mimes
        this.response.writer.print(data)
        this.response.flushBuffer()
        if (mimes == Mimes.JSON) {
            logd("Response JSON: ", data)
        }
    }

    fun send(mimes: String, block: PrintWriter.() -> Unit) {
        this.response.contentType = mimes
        val w = this.response.writer
        w.block()
        this.response.flushBuffer()
    }

    fun sendText(text: String) {
        send(Mimes.PLAIN, text)
    }

    fun sendJSON(jsonText: String) {
        send(Mimes.JSON, jsonText)
    }

    fun sendXML(xmlText: String) {
        send(Mimes.XML, xmlText)
    }

    fun sendHTML(htmlText: String) {
        send(Mimes.HTML, htmlText)
    }

    fun sendData(data: ByteArray, contentType: String) {
        this.response.contentType = contentType
        this.response.outputStream.write(data)
        this.response.flushBuffer()
    }

    fun sendDataAttach(filename: String, data: ByteArray, contentType: String = Mimes.ofFile(filename)) {
        this.response.contentType = contentType
        this.response.addHeader("Content-Disposition", "attachment;filename=$filename")
        this.response.addHeader("Content-Length", data.size.toString())
        this.response.outputStream.write(data)
        this.response.flushBuffer()
    }

    fun nextChain() {
        chain.doFilter(request, response)
    }

    fun allowCross() {
        val origin = this.header("Origin")
        if (origin != null) {
            this.response.setHeader("Access-Control-Allow-Origin", origin)
            this.response.setHeader("Access-Control-Allow-Credentials", "true")
            this.response.setHeader("Access-Control-Allow-Methods", "GET,POST,HEAD,OPTIONS")
            this.response.setHeader(
                "Access-Control-Allow-Headers",
                "Origin,Accept,Content-Type,Content-Length,X-Requested-With,Key,Token,Authorization"
            )

        }
    }

    fun webappFile(apppath: String): File {
        return File(httpService.appDir, apppath)
    }
}
