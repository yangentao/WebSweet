package dev.entao.web.core

val HttpContext.lastEventID: String? get() = this.request.header("Last-Event-ID")

fun HttpContext.sendSSE(model: SSEModel) {
    this.response.contentType = "text/event-stream;charset=utf-8"
    val w = this.response.writer
    if (model.id.isNotEmpty()) w.println("id:${model.id}")
    if (model.event.isNotEmpty()) w.println("event:${model.event}")
    if (model.data.isNotEmpty()) w.println("data:${model.data}")
    if (model.retry.isNotEmpty()) w.println("retry:${model.retry}")
    w.println()
    this.response.flushBuffer()
}

fun HttpContext.sendSSE(block: SSEModel.() -> Unit) {
    val m = SSEModel()
    m.block()
    sendSSE(m)
}

class SSEModel {
    var event: String = ""
    var id: String = ""
    var data: String = ""
    var retry: String = "5000"

    override fun toString(): String {
        val sb = StringBuilder()
        if (event.isNotEmpty()) sb.append("event:").append(event).appendLine()
        if (id.isNotEmpty()) sb.append("id:").append(id).appendLine()
        if (data.isNotEmpty()) sb.append("data:").append(data).appendLine()
        if (retry.isNotEmpty()) sb.append("retry:").append(retry).appendLine()
        sb.appendLine()
        return sb.toString()
    }
}