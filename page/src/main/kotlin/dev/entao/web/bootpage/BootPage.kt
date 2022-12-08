package dev.entao.web.bootpage

import dev.entao.web.core.HttpContext
import dev.entao.web.core.OnHttpContext
import dev.entao.web.core.ResAcceptor
import dev.entao.web.core.uri
import dev.entao.web.tag.HtmlPage
import dev.entao.web.tag.tag.link
import dev.entao.web.tag.tag.meta
import dev.entao.web.tag.tag.script


open class BootPage(context: HttpContext) : HtmlPage(context) {

    init {
        html.lang = "zh"
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width,initial-scale=1")
            meta(name = "description", content = "")
            title("Title")
            link(rel = "stylesheet", href = "bootstrap.css".pageRes)
        }

        body {
            script(src = "bootstrap.js".pageRes)
        }

    }

    companion object {
        val bootAcceptor: ResAcceptor = { s -> s.startsWith("page/") }
    }
}

context (OnHttpContext)
val String.pageRes: String
    get() = "@res/page/$this".uri