package dev.entao.web.core.render

import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext

abstract class Render(override val context: HttpContext) : OnHttpContext {


    fun send() {
        onSend()
        if (!context.isCommited) {
            context.response.flushBuffer()
        }
    }

    protected abstract fun onSend()


}
