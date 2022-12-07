package dev.entao.web.bootpage

import dev.entao.web.core.HttpContext
import dev.entao.web.core.ResAcceptor
import dev.entao.web.core.uri
import dev.entao.web.tag.HtmlPage
import dev.entao.web.tag.tag.link
import dev.entao.web.tag.tag.meta
import dev.entao.web.tag.tag.script

//TODO @res, @static, @app, @
open class BootPage(context: HttpContext) : HtmlPage(context) {

    init {
        head {
            meta(charset = "utf-8")
            meta(name = "viewport", content = "width=device-width,initial-scale=1")
            meta(name = "description", content = "")
            title("Dashboard")
            link(rel = "stylesheet", href = "@res/bootpage/bootstrap.css".uri)
        }

        body {
            script(src = "@res/bootpage/bootstrap.js".uri)
        }

    }

    companion object {
        val bootAcceptor: ResAcceptor = { s -> s.startsWith("bootpage/") }
    }
}