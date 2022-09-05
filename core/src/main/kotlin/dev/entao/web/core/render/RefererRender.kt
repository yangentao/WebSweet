@file:Suppress("MemberVisibilityCanBePrivate")

package dev.entao.web.core.render

import dev.entao.web.base.urlEncoded
import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.core.contentTypeHtml
import dev.entao.web.core.referer

class RefererRender(context: HttpContext) : Render(context) {
    val url = context.request.referer?.substringBefore('?') ?: kotlin.error("no referer")
    val paramMap = LinkedHashMap<String, ArrayList<String>>()

    init {
        context.response.contentTypeHtml()
        val map = context.paramMap
        for ((k, ar) in map) {
            paramMap[k] = ArrayList(ar)
        }
    }

    fun clearParams() {
        paramMap.clear()
    }

    fun removeKey(key: String) {
        paramMap.remove(key)
    }

    fun setParam(key: String, value: String) {
        paramMap[key] = arrayListOf(value)
    }

    fun addParam(key: String, value: String) {
        val ls = paramMap[key] ?: ArrayList()
        ls += value
        paramMap[key] = ls
    }

    fun error(msg: String) {
        paramMap[ERROR_MSG] = arrayListOf(msg)
    }

    fun success(msg: String) {
        paramMap[SUCCESS_MSG] = arrayListOf(msg)
    }

    fun errorFieldName(name: String?, msg: String = "") {
        val k = name ?: return
        paramMap[fieldError(k)] = arrayListOf(msg)
    }

    override fun onSend() {
        val list = ArrayList<String>()
        for ((k, ls) in paramMap) {
            for (s in ls) {
                list += "${k.urlEncoded}=${s.urlEncoded}"
            }
        }
        val query = list.joinToString("&")
        if (query.isEmpty()) {
            context.response.sendRedirect(url)
        } else {
            context.response.sendRedirect("$url?$query")
        }
    }

    companion object {
        const val ERROR_MSG = "back_error_msg"
        const val SUCCESS_MSG = "back_success_msg"

        fun fieldError(name: String): String {
            return name + "_error_field"
        }
    }

}

context (OnHttpContext)
fun sendBack(block: RefererRender.() -> Unit) {
    context.sendBack(block)
}

fun HttpContext.sendBack(block: RefererRender.() -> Unit) {
    val p = RefererRender(this)
    p.block()
    p.send()
}

val HttpContext.backErrorMessage: String? get() = this[RefererRender.ERROR_MSG]
val HttpContext.backSuccessMessage: String? get() = this[RefererRender.SUCCESS_MSG]
fun HttpContext.isFieldError(name: String): Boolean {
    return this[RefererRender.fieldError(name)] != null
}