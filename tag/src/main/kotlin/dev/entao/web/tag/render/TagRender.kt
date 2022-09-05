package dev.entao.web.tag.render

import dev.entao.web.base.Mimes
import dev.entao.web.core.HttpContext
import dev.entao.web.core.render.Render
import dev.entao.web.tag.tag.Tag
import dev.entao.web.tag.tag.scriptsToBottom

abstract class TagRender(context: HttpContext) : Render(context) {
    abstract val renderTag: Tag
    open val contentType: String = Mimes.HTML

    override fun onSend() {
        renderTag.scriptsToBottom()
        context.send(contentType) {
            renderTag.toHtml(this)
        }
    }

}