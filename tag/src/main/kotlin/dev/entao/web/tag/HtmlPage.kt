package dev.entao.web.tag

import dev.entao.web.base.Mimes
import dev.entao.web.core.HttpContext
import dev.entao.web.tag.render.TagRender
import dev.entao.web.tag.tag.BodyTag
import dev.entao.web.tag.tag.HeadTag
import dev.entao.web.tag.tag.HtmlTag
import dev.entao.web.tag.tag.Tag
import dev.entao.web.tag.tag.TagBlock
import dev.entao.web.tag.tag.scriptsToBottom
import dev.entao.web.tag.tag.text

open class HtmlPage(context: HttpContext, val html: HtmlTag = HtmlTag(context)) : TagRender(context) {
    override val renderTag: Tag get() = html
    val head: HeadTag = html.append(HeadTag(context))
    val body: BodyTag = html.append(BodyTag(context))

    fun head(block: TagBlock) {
        this.head.block()
    }

    fun body(block: TagBlock) {
        this.body.block()
    }

    fun title(s: String) {
        val a = head.single("title")
        a.children.clear()
        a.text(s)
    }

    override fun onSend() {
        body.scriptsToBottom()
        this.context.send(Mimes.HTML) {
            println("<!DOCTYPE HTML>")
            html.toHtml(this)
        }
    }

}

