@file:Suppress("unused")

package dev.entao.web.core.render

import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.json.YsonArray
import dev.entao.web.json.YsonObject
import dev.entao.web.json.ysonArray

class Result {
    val jo = YsonObject()

    fun success(msg: String = MSG_OK) {
        jo.putInt(CODE, CODE_OK)
        jo.putString(MSG, msg)
    }

    fun failed(msg: String, code: Int = -1) {
        jo.putInt(CODE, code)
        jo.putString(MSG, msg)
    }

    fun code(code: Int, msg: String) {
        jo.putInt(CODE, code)
        jo.putString(MSG, msg)
    }

    fun put(key: String, value: Any) {
        jo.putAny(key, value)
    }

    fun data(n: Int) {
        jo.putInt(DATA, n)
    }

    fun data(lv: Long) {
        jo.putAny(DATA, lv)
    }

    fun data(f: Float) {
        jo.putAny(DATA, f)
    }

    fun data(v: Double) {
        jo.putAny(DATA, v)
    }

    fun data(s: String) {
        jo.putString(DATA, s)
    }

    fun data(yo: YsonObject) {
        jo.putAny(DATA, yo)
    }

    fun data(ya: YsonArray) {
        jo.putAny(DATA, ya)
    }

    fun <T : Any> dataArray(dataList: Collection<T>, block: (T) -> Any?) {
        val ya = ysonArray(dataList, block)
        jo.putAny(DATA, ya)
    }

    fun data(block: YsonObject.() -> Unit) {
        val yo = YsonObject()
        data(yo)
        yo.block()
    }

    override fun toString(): String {
        return jo.toString()
    }

    companion object {
        const val CODE_OK = 0
        const val MSG_OK = "操作成功"
        const val MSG_FAILED = "操作失败"

        var MSG = "msg"
        var CODE = "code"
        var DATA = "data"

    }
}

fun HttpContext.sendResult(block: Result.() -> Unit) {
    val r = Result()
    r.success()
    r.block()
    this.sendJSON(r.jo.toString())
}

fun HttpContext.sendFailed(code: Int, msg: String = Result.MSG_FAILED) {
    sendResult {
        code(code, msg)
    }
}

fun HttpContext.sendSuccess(msg: String = Result.MSG_OK) {
    sendResult {
        success(msg)
    }
}

fun HttpContext.sendObject(yo: YsonObject) {
    sendResult {
        data(yo)
    }
}

fun HttpContext.sendObject(block: YsonObject.() -> Unit) {
    sendResult {
        data(block)
    }
}

fun HttpContext.sendArray(ya: YsonArray) {
    sendResult {
        data(ya)
    }
}

fun <T : Any> HttpContext.sendArray(dataList: Collection<T>, block: (T) -> Any?) {
    sendResult {
        dataArray(dataList, block)
    }
}

context (OnHttpContext)
fun sendResult(block: Result.() -> Unit) {
    context.sendResult(block)
}

context (OnHttpContext)
fun sendFailed(code: Int, msg: String = Result.MSG_FAILED) {
    context.sendFailed(code, msg)
}

context (OnHttpContext)
fun sendSuccess(msg: String = Result.MSG_OK) {
    context.sendSuccess(msg)
}

context (OnHttpContext)
fun sendObject(yo: YsonObject) {
    context.sendObject(yo)
}

context (OnHttpContext)
fun sendObject(block: YsonObject.() -> Unit) {
    context.sendObject(block)
}

context (OnHttpContext)
fun sendArray(ya: YsonArray) {
    context.sendArray(ya)
}

context (OnHttpContext)
fun <T : Any> sendArray(dataList: Collection<T>, block: (T) -> Any?) {
    context.sendArray(dataList, block)
}

