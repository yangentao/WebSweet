@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PropertyName", "EXPERIMENTAL_API_USAGE", "OPT_IN_USAGE")

import org.khronos.webgl.ArrayBuffer
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.url.URLSearchParams
import org.w3c.files.Blob
import org.w3c.xhr.*
import kotlin.js.Json

//
//xxx.httpGet(url) {
//	arg("user", "yang")
//	arg("pwd", "123")
//	onLoad { r ->
//		console.info(r.textValue)
//	}
//}

@JsExport
@JsName("HttpResponse")
class HttpResponse(val xhr: XMLHttpRequest) {

    val OK: Boolean get() = status in 200..299
    val status: Int get() = xhr.status.toInt()
    val msg: String get() = xhr.statusText
    val contentType: String = xhr.getResponseHeader("Content-Type") ?: ""
    val responseType: XMLHttpRequestResponseType get() = xhr.responseType

    val isJSON: Boolean get() = xhr.responseType == XMLHttpRequestResponseType.JSON
    val isText: Boolean get() = xhr.responseType == XMLHttpRequestResponseType.TEXT || xhr.responseType == XMLHttpRequestResponseType.EMPTY
    val isDocument: Boolean get() = xhr.responseType == XMLHttpRequestResponseType.DOCUMENT
    val isBlob: Boolean get() = xhr.responseType == XMLHttpRequestResponseType.BLOB
    val isArrayBuffer: Boolean get() = xhr.responseType == XMLHttpRequestResponseType.ARRAYBUFFER

    fun header(key: String): String? {
        return xhr.getResponseHeader(key)
    }

    val blobValue: Blob? get() = xhr.response as? Blob
    val bufferValue: ArrayBuffer? get() = xhr.response as? ArrayBuffer
    val textValue: String get() = xhr.responseText
    val docValue: Document? get() = xhr.responseXML
    val jsonValue: Json
        get() {
            @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
            if (isJSON) return xhr.response as Json
            if (isText) return JSON.parse(xhr.responseText)
            throw Exception("type error")
        }

    val jsonResult: JsonResult
        get() {
            return JsonResult(jsonValue)
        }

}

@JsExport
@JsName("HttpGet")
class HttpGet(url: String) : HttpRequest(url, "GET") {
    override fun request() {
        val sp = searchParams.toString()
        val urlX = if (sp.isEmpty()) {
            url
        } else {
            "$url?$sp"
        }
        xhr.open(method, urlX, true)
        for ((k, v) in headerMap) {
            xhr.setRequestHeader(k, v)
        }
        xhr.send(null)
    }
}

@JsExport
@JsName("HttpPost")
class HttpPost(url: String) : HttpRequest(url, "POST") {

    //FormData时, content-type头信息会被xhr自动设置
    var bodyData: dynamic = null

    fun data(data: dynamic): HttpPost {
        this.bodyData = data
        return this
    }

    fun formData(block: FormData.() -> Unit): HttpPost {
        bodyData = FormData().apply(block)
        return this
    }

    fun formContent(): HttpPost {
        contentType("application/x-www-form-urlencoded")
        return this
    }

    fun jsonContent(): HttpPost {
        contentType("application/json")
        return this
    }

    fun xmlContent(): HttpPost {
        contentType("application/xml")
        return this
    }

    fun multipart(): HttpPost {
        contentType("multipart/form-data")
        return this
    }

    fun contentType(t: String): HttpPost {
        header("Content-Type", t)
        return this
    }

    override fun request() {
        val sp = searchParams.toString()
        val urlX = if (bodyData != null && sp.isNotEmpty()) {
            "$url?$sp"
        } else {
            url
        }

        if (bodyData is FormData) {
            headerMap.remove("Content-Type")
        } else if (headerMap["Content-Type"] == null) {
            if (sp.isNotEmpty()) {
                formContent()
            }
        }

        xhr.open(method, urlX, true)

        for ((k, v) in headerMap) {
            xhr.setRequestHeader(k, v)
        }
        val dd = when {
            bodyData != null -> {
                bodyData
            }

            sp.isNotEmpty() -> {
                sp
            }

            else -> null
        }
        xhr.send(dd)

    }
}

typealias HttpResultCallback = (HttpResponse) -> Unit

@JsExport
@JsName("HttpRequest")
abstract class HttpRequest(urlString: String, val method: String) {
    val xhr = XMLHttpRequest().apply {
        timeout = 15_000
    }
    val url: String
    protected val headerMap: HashMap<String, String> = HashMap()
    protected val searchParams: URLSearchParams

    private var successCallback: HttpResultCallback? = null
    private var failedCallback: HttpResultCallback? = null
    private var loadCallback: HttpResultCallback? = null

    init {
        this.url = urlString.substringBefore('?')
        val params: String = urlString.substringAfter('?', "")
        searchParams = if (params.isEmpty()) {
            URLSearchParams()
        } else {
            URLSearchParams(params)
        }
        xhr.addEventListener("load", {
            val resp = HttpResponse(xhr)
            if (resp.OK) {
                successCallback?.invoke(resp)
            } else {
                failedCallback?.invoke(resp)
            }
            loadCallback?.invoke(resp)
        })

    }

    fun onSuccess(block: HttpResultCallback): HttpRequest {
        this.successCallback = block
        return this
    }

    fun onFailed(block: HttpResultCallback): HttpRequest {
        this.failedCallback = block
        return this
    }

    fun onLoad(block: HttpResultCallback): HttpRequest {
        this.loadCallback = block
        return this
    }

    abstract fun request()

    fun requestJsonResult(successCallback: (JsonResult) -> Unit) {
        jsonResponse()
        onSuccess {
            successCallback(it.jsonResult)
        }
        request()
    }

    fun requestJson(successCallback: (Json) -> Unit) {
        jsonResponse()
        onSuccess {
            successCallback(it.jsonValue)
        }
        request()
    }

    fun requestText(successCallback: (String) -> Unit) {
        textResponse()
        onSuccess {
            successCallback(it.textValue)
        }
        request()
    }

    fun onProgressUpload(block: (ProgressEvent) -> Unit): HttpRequest {
        xhr.upload.onprogress = block
        return this
    }

    fun onProgress(block: (ProgressEvent) -> Unit): HttpRequest {
        xhr.onprogress = block
        return this
    }

    fun jsonResponse(): HttpRequest {
        xhr.responseType = XMLHttpRequestResponseType.JSON
        return this
    }

    fun textResponse(): HttpRequest {
        xhr.responseType = XMLHttpRequestResponseType.TEXT
        return this
    }

    fun docResponse(): HttpRequest {
        xhr.responseType = XMLHttpRequestResponseType.DOCUMENT
        return this
    }

    fun blobResponse(): HttpRequest {
        xhr.responseType = XMLHttpRequestResponseType.BLOB
        return this
    }

    fun param(key: String, value: String): HttpRequest {
        searchParams.set(key, value)
        return this
    }

    fun appendParam(key: String, value: String): HttpRequest {
        searchParams.append(key, value)
        return this
    }

    fun header(key: String, value: String): HttpRequest {
        headerMap[key] = value
        return this
    }

    fun abort() {
        xhr.onreadystatechange = {}
        xhr.abort()
    }
}

fun <T : HttpRequest> T.params(vararg kvs: Pair<String, Any>): T {
    for (p in kvs) {
        this.param(p.first, p.second.toString())
    }
    return this
}

fun <T : HttpRequest> T.withCredentials(): T {
    this.xhr.withCredentials = true
    return this
}

fun <T : HttpRequest> T.withToken(token: String?): T {
    val tk = token ?: return this
    if (tk.isNotEmpty()) {
        this.header("Authorization", "Bearer $tk")
    }
    return this
}

@JsExport
@JsName("httpLoad")
fun httpLoad(ele: Element, url: String, block: HttpGet.() -> Unit) {
    val h = HttpGet(url).apply(block)
    h.requestText {
        ele.htmlValue = it
    }
}

fun Element.load(url: String, block: HttpGet.() -> Unit) {
    httpLoad(this, url, block)
}

fun httpJsonResult(url: String, vararg ps: Pair<String, String>, blockSuccess: (JsonResult) -> Unit) {
    HttpGet(url).apply {
        jsonResponse()
        for (p in ps) {
            this.param(p.first, p.second)
        }
        onSuccess {
            blockSuccess(it.jsonResult)
        }
    }.request()
}
