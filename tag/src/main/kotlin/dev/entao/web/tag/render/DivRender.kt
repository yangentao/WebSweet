package dev.entao.web.tag.render

import dev.entao.web.base.Mimes
import dev.entao.web.core.HttpContext
import dev.entao.web.tag.tag.DivTag
import dev.entao.web.tag.tag.Tag

open class DivRender(context: HttpContext, val rootDiv: DivTag = DivTag(context)) : TagRender(context) {

    override val renderTag: Tag get() = rootDiv
    override val contentType: String = Mimes.HTML

    fun build(block: DivTag.() -> Unit): DivRender {
        rootDiv.block()
        return this
    }
}